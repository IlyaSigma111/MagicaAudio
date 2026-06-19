package com.example.magica.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import com.example.magica.model.AudioSettings
import com.example.magica.model.TrackData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ShortBuffer

object AudioProcessor {

    suspend fun processTrack(
        context: Context,
        track: TrackData,
        onProgress: (Int) -> Unit = {},
    ): String? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = AudioUtils.getCacheDir(context)
            val outputFile = File(cacheDir, "track_${track.id}_processed.wav")

            val pcmData = decodeToPCM(context, Uri.fromFile(File(track.filePath)))
                ?: decodeRawWav(track.filePath)
                ?: return@withContext null

            var samples = pcmData.first
            val sampleRate = pcmData.second

            onProgress(10)

            samples = DspEffects.applyHighPass(samples, track.settings.highPassCutoff, sampleRate)
            samples = DspEffects.applyLowPass(samples, track.settings.lowPassCutoff, sampleRate)

            onProgress(25)

            samples = DspEffects.applyPitchShift(samples, track.settings.pitchSemitones, sampleRate)
            samples = DspEffects.applySpeedChange(samples, track.settings.speedRatio)

            onProgress(40)

            samples = DspEffects.applyEqualizer(samples, track.settings.eqBands, track.settings.eqFrequencies, sampleRate)

            onProgress(55)

            samples = DspEffects.applyChorus(samples, track.settings.chorusMix, sampleRate)

            onProgress(65)

            samples = DspEffects.applyReverb(samples, track.settings.reverbMix, track.settings.reverbRoomSize, track.settings.reverbDamping, sampleRate)

            onProgress(75)

            samples = DspEffects.applyDistortion(samples, track.settings.distortionGain)

            if (track.settings.robotEffect) {
                samples = DspEffects.applyRobotEffect(samples, sampleRate)
            }

            onProgress(85)

            samples = DspEffects.applyVolume(samples, track.settings.volume * track.volume)

            onProgress(90)

            writeWav(outputFile.absolutePath, samples, sampleRate)

            onProgress(100)
            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun processTrackSegment(
        context: Context,
        track: TrackData,
        segmentDurationSec: Int = 5,
    ): String? = withContext(Dispatchers.IO) {
        try {
            val cacheDir = AudioUtils.getCacheDir(context)
            val outputFile = File(cacheDir, "track_${track.id}_preview.wav")

            val pcmData = decodeToPCM(context, Uri.fromFile(File(track.filePath)), segmentDurationSec * 1000)
                ?: return@withContext null

            var samples = pcmData.first
            val sampleRate = pcmData.second

            samples = DspEffects.applyHighPass(samples, track.settings.highPassCutoff, sampleRate)
            samples = DspEffects.applyLowPass(samples, track.settings.lowPassCutoff, sampleRate)

            samples = DspEffects.applyPitchShift(samples, track.settings.pitchSemitones, sampleRate)
            samples = DspEffects.applySpeedChange(samples, track.settings.speedRatio)

            samples = DspEffects.applyEqualizer(samples, track.settings.eqBands, track.settings.eqFrequencies, sampleRate)

            samples = DspEffects.applyChorus(samples, track.settings.chorusMix, sampleRate)

            samples = DspEffects.applyReverb(samples, track.settings.reverbMix, track.settings.reverbRoomSize, track.settings.reverbDamping, sampleRate)

            samples = DspEffects.applyDistortion(samples, track.settings.distortionGain)

            if (track.settings.robotEffect) {
                samples = DspEffects.applyRobotEffect(samples, sampleRate)
            }

            samples = DspEffects.applyVolume(samples, track.settings.volume * track.volume)

            writeWav(outputFile.absolutePath, samples, sampleRate)
            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun mixTracks(
        context: Context,
        tracks: List<TrackData>,
        onProgress: (Int) -> Unit = {},
    ): String? = withContext(Dispatchers.IO) {
        if (tracks.isEmpty()) return@withContext null
        if (tracks.size == 1) {
            return@withContext processTrack(context, tracks[0], onProgress)
        }

        val processedFiles = mutableListOf<String>()
        for ((i, track) in tracks.withIndex()) {
            onProgress((i * 100) / tracks.size)
            val result = processTrack(context, track) ?: return@withContext null
            processedFiles.add(result)
        }

        val cacheDir = AudioUtils.getCacheDir(context)
        val mixedFile = File(cacheDir, "mixed_output.wav")

        var maxLen = 0
        var sampleRate = 44100
        val allSamples = mutableListOf<ShortArray>()
        for (file in processedFiles) {
            val pcm = readWav(file)
            if (pcm != null) {
                allSamples.add(pcm.first)
                maxLen = maxOf(maxLen, pcm.first.size)
                sampleRate = pcm.second
            }
        }

        if (allSamples.isEmpty()) return@withContext null

        val mixed = ShortArray(maxLen)
        for (i in 0 until maxLen) {
            var sum = 0f
            for (samples in allSamples) {
                if (i < samples.size) {
                    sum += samples[i] / 32768f
                }
            }
            mixed[i] = (sum * 32768f / allSamples.size)
                .toInt().coerceIn(-32768, 32767).toShort()
        }

        onProgress(90)
        writeWav(mixedFile.absolutePath, mixed, sampleRate)
        onProgress(100)
        mixedFile.absolutePath
    }

    suspend fun exportMix(
        context: Context,
        tracks: List<TrackData>,
        format: String = "mp3",
        bitrate: String = "192k",
        onProgress: (Int) -> Unit = {},
    ): String? = withContext(Dispatchers.IO) {
        val mixed = mixTracks(context, tracks, onProgress) ?: return@withContext null
        val exportDir = AudioUtils.getOutputDir(context)
        val ext = if (format == "wav") "wav" else "mp3"
        val exportFile = File(exportDir, "export_${System.currentTimeMillis()}.$ext")

        if (format == "wav") {
            File(mixed).copyTo(exportFile, overwrite = true)
        } else {
            encodeToMp3(context, mixed, exportFile.absolutePath)
        }
        exportFile.absolutePath
    }

    fun extractWaveformPCM(filePath: String, targetSamples: Int = 500): FloatArray? {
        return try {
            val wavData = readWav(filePath) ?: return null
            val samples = wavData.first

            val chunkSize = samples.size.coerceAtLeast(1) / targetSamples.coerceAtLeast(1)
            val result = FloatArray(targetSamples)
            for (i in 0 until targetSamples) {
                var maxVal = 0f
                val start = i * chunkSize
                val end = minOf(start + chunkSize, samples.size)
                for (j in start until end) {
                    val abs = kotlin.math.abs(samples[j] / 32768f)
                    if (abs > maxVal) maxVal = abs
                }
                result[i] = maxVal
            }
            result
        } catch (_: Exception) { null }
    }

    fun getFileDurationMs(filePath: String): Long {
        return try {
            val wavData = readWav(filePath) ?: return 0L
            (wavData.first.size.toLong() * 1000 / wavData.second)
        } catch (_: Exception) { 0L }
    }

    private fun decodeToPCM(context: Context, uri: Uri, maxMs: Long? = null): Pair<ShortArray, Int>? {
        return try {
            val extractor = MediaExtractor()
            extractor.setDataSource(context, uri, null)

            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                if (format.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                    audioTrackIndex = i
                    audioFormat = format
                    break
                }
            }

            if (audioTrackIndex < 0) {
                extractor.release()
                return null
            }

            val sampleRate = audioFormat!!.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = audioFormat!!.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val mime = audioFormat!!.getString(MediaFormat.KEY_MIME) ?: "audio/raw"

            if (mime == "audio/raw") {
                extractor.selectTrack(audioTrackIndex)
                val pcmData = mutableListOf<Short>()
                val buf = ByteBuffer.allocate(65536)
                while (true) {
                    val sampleSize = extractor.readSampleData(buf, 0)
                    if (sampleSize < 0) break
                    buf.rewind()
                    buf.limit(sampleSize)
                    val shortBuf = buf.order(ByteOrder.nativeOrder()).asShortBuffer()
                    while (shortBuf.hasRemaining()) {
                        pcmData.add(shortBuf.get())
                    }
                    buf.clear()
                    extractor.advance()
                }
                extractor.release()

                val mono = if (channelCount > 1) {
                    val m = ShortArray(pcmData.size / channelCount)
                    for (i in m.indices) {
                        var sum = 0
                        for (ch in 0 until channelCount) {
                            sum += pcmData[i * channelCount + ch]
                        }
                        m[i] = (sum / channelCount).toShort()
                    }
                    m
                } else {
                    pcmData.toShortArray()
                }
                return Pair(mono, sampleRate)
            }

            val decoder = MediaCodec.createDecoderByType(mime)
            decoder.configure(audioFormat, null, null, 0)
            decoder.start()

            extractor.selectTrack(audioTrackIndex)

            val pcmData = mutableListOf<Short>()
            val bufferInfo = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            val timeoutUs = 10000L
            val maxBytes = if (maxMs != null) {
                (maxMs * sampleRate * 2 / 1000).toInt()
            } else Int.MAX_VALUE

            while (!outputDone) {
                if (!inputDone) {
                    val inputIndex = decoder.dequeueInputBuffer(timeoutUs)
                    if (inputIndex >= 0) {
                        val inputBuf = decoder.getInputBuffer(inputIndex)!!
                        val sampleSize = extractor.readSampleData(inputBuf, 0)
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            decoder.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                val outputIndex = decoder.dequeueOutputBuffer(bufferInfo, timeoutUs)
                if (outputIndex >= 0) {
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        outputDone = true
                    }
                    if (bufferInfo.size > 0) {
                        val outputBuf = decoder.getOutputBuffer(outputIndex)!!
                        outputBuf.rewind()
                        outputBuf.limit(bufferInfo.size)
                        val shortBuf = outputBuf.order(ByteOrder.nativeOrder()).asShortBuffer()
                        while (shortBuf.hasRemaining() && pcmData.size < maxBytes) {
                            pcmData.add(shortBuf.get())
                        }
                    }
                    decoder.releaseOutputBuffer(outputIndex, false)
                } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    continue
                }
            }

            decoder.stop()
            decoder.release()
            extractor.release()

            val mono = if (channelCount > 1) {
                val m = ShortArray(pcmData.size / channelCount)
                for (i in m.indices) {
                    var sum = 0
                    for (ch in 0 until channelCount) {
                        sum += pcmData[i * channelCount + ch]
                    }
                    m[i] = (sum / channelCount).toShort()
                }
                m
            } else {
                pcmData.toShortArray()
            }

            Pair(mono, sampleRate)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun decodeRawWav(filePath: String): Pair<ShortArray, Int>? {
        return readWav(filePath)
    }

    private fun readWav(filePath: String): Pair<ShortArray, Int>? {
        return try {
            val file = File(filePath)
            if (!file.exists() || !file.name.endsWith(".wav", ignoreCase = true)) return null

            val input = FileInputStream(file)
            val header = ByteArray(44)
            if (input.read(header) < 44) {
                input.close()
                return null
            }

            val sampleRate = ((header[24].toInt() and 0xFF) or
                    ((header[25].toInt() and 0xFF) shl 8) or
                    ((header[26].toInt() and 0xFF) shl 16) or
                    ((header[27].toInt() and 0xFF) shl 24))

            val bitsPerSample = ((header[34].toInt() and 0xFF) or
                    ((header[35].toInt() and 0xFF) shl 8))

            val channels = ((header[22].toInt() and 0xFF) or
                    ((header[23].toInt() and 0xFF) shl 8))

            val dataSize = file.length().toInt() - 44
            val rawData = ByteArray(dataSize)
            input.read(rawData)
            input.close()

            val shorts: ShortArray
            if (bitsPerSample == 16) {
                shorts = ShortArray(dataSize / 2)
                for (i in shorts.indices) {
                    val lo = rawData[i * 2].toInt() and 0xFF
                    val hi = rawData[i * 2 + 1].toInt()
                    shorts[i] = (lo or (hi shl 8)).toShort()
                }
            } else if (bitsPerSample == 8) {
                shorts = ShortArray(dataSize)
                for (i in shorts.indices) {
                    shorts[i] = ((rawData[i].toInt() and 0xFF) - 128).toShort()
                }
            } else {
                return null
            }

            val mono = if (channels > 1) {
                val m = ShortArray(shorts.size / channels)
                for (i in m.indices) {
                    var sum = 0
                    for (ch in 0 until channels) {
                        sum += shorts[i * channels + ch]
                    }
                    m[i] = (sum / channels).toShort()
                }
                m
            } else shorts

            Pair(mono, sampleRate)
        } catch (e: Exception) { null }
    }

    private fun writeWav(filePath: String, samples: ShortArray, sampleRate: Int) {
        val dataSize = samples.size * 2
        val fileSize = 44 + dataSize

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray())
        header.putInt(fileSize - 8)
        header.put("WAVE".toByteArray())
        header.put("fmt ".toByteArray())
        header.putInt(16)
        header.putShort(1)
        header.putShort(1)
        header.putInt(sampleRate)
        header.putInt(sampleRate * 2)
        header.putShort(2)
        header.putShort(16)
        header.put("data".toByteArray())
        header.putInt(dataSize)

        val output = FileOutputStream(filePath)
        output.write(header.array())

        val dataBuf = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
        for (s in samples) {
            dataBuf.putShort(s)
        }
        output.write(dataBuf.array())
        output.close()
    }

    private fun encodeToMp3(context: Context, inputWavPath: String, outputMp3Path: String) {
        try {
            val wavData = readWav(inputWavPath) ?: return
            val samples = wavData.first
            val sampleRate = wavData.second

            val mediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIME_TYPE_AUDIO_AAC, sampleRate, 1)
            mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaFormat.AACObjectLC)
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 192000)

            val muxer = MediaMuxer(outputMp3Path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val trackIndex = muxer.addTrack(mediaFormat)
            muxer.start()

            val codec = MediaCodec.createEncoderByType(MediaFormat.MIME_TYPE_AUDIO_AAC)
            codec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            codec.start()

            val bufferInfo = MediaCodec.BufferInfo()
            var presentationTimeUs = 0L
            val sampleSize = 1024
            var inputIndex = 0
            var outputDone = false

            while (inputIndex < samples.size || !outputDone) {
                val inputBufIndex = codec.dequeueInputBuffer(10000)
                if (inputBufIndex >= 0 && inputIndex < samples.size) {
                    val inputBuf = codec.getInputBuffer(inputBufIndex)!!
                    inputBuf.clear()
                    val remaining = minOf(sampleSize, samples.size - inputIndex)
                    val shortBuf = inputBuf.asShortBuffer()
                    for (i in 0 until remaining) {
                        shortBuf.put(samples[inputIndex + i])
                    }
                    inputBuf.limit(remaining * 2)
                    codec.queueInputBuffer(inputBufIndex, 0, remaining * 2,
                        presentationTimeUs,
                        if (inputIndex + remaining >= samples.size) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0)
                    inputIndex += remaining
                    presentationTimeUs += (remaining * 1000000L / sampleRate)
                }

                val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputIndex >= 0) {
                    val outputBuf = codec.getOutputBuffer(outputIndex)!!
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        outputDone = true
                    }
                    if (bufferInfo.size > 0) {
                        outputBuf.position(bufferInfo.offset)
                        outputBuf.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(trackIndex, outputBuf, bufferInfo)
                    }
                    codec.releaseOutputBuffer(outputIndex, false)
                } else if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    if (inputIndex >= samples.size) outputDone = true
                }
            }

            codec.stop()
            codec.release()
            muxer.stop()
            muxer.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
