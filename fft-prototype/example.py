#!/usr/bin/env python
import numpy as np
import pyaudio


FRAME_SIZE = 1024
SAMPLE_RATE = 44100


def freq_to_idx(freq):
    return int(round(float(freq) / SAMPLE_RATE * FRAME_SIZE))


def idx_to_freq(idx):
    return float(idx) * SAMPLE_RATE / FRAME_SIZE


def main():
    player = pyaudio.PyAudio()
    stream = player.open(format=player.get_format_from_width(2),
                         channels=1,
                         rate=SAMPLE_RATE,
                         input=True,
                         frames_per_buffer=FRAME_SIZE)

    try:
        target_freq_min = 19000
        target_freq_max = 21000
        target_idx_min = freq_to_idx(target_freq_min)
        target_idx_max = freq_to_idx(target_freq_max)
        percentage = 0.25

        while True:
            signal = stream.read(FRAME_SIZE)
            wave_data = np.fromstring(signal, dtype=np.int16) / 32768.
            freq_data = abs(np.fft.rfft(wave_data))

            rank_data = np.searchsorted(np.sort(freq_data), freq_data)
            idx = rank_data[target_idx_min:target_idx_max+1].argmax()
            idx += target_idx_min

            if rank_data[idx] + 1 >= len(freq_data) * (1.0 - percentage):
                print 'GOOD %8.2f' % idx_to_freq(idx)
            else:
                print ''

    except KeyboardInterrupt, SystemExit:
        stream.stop_stream()
        stream.close()
        player.terminate()


if __name__ == '__main__':
    main()
