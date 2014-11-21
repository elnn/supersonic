#!/usr/bin/env python
import numpy as np
import pyaudio


def main():
    frame_size = 1024
    sample_rate = 44100
    player = pyaudio.PyAudio()
    stream = player.open(format=player.get_format_from_width(2),
                         channels=1,
                         rate=sample_rate,
                         input=True,
                         frames_per_buffer=frame_size)

    try:
        while True:
            signal = stream.read(frame_size)
            wave_data = np.fromstring(signal, dtype=np.int16) / 32768.
            freq_data = abs(np.fft.rfft(wave_data)) ** 2
            peak = freq_data.argmax()
            frequency = float(peak) * sample_rate / frame_size
            print '%8.2f' % frequency

    except KeyboardInterrupt, SystemExit:
        stream.stop_stream()
        stream.close()
        player.terminate()


if __name__ == '__main__':
    main()
