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

    target_freq_min = 19000
    target_freq_max = 21000
    target_idx_min = freq_to_idx(target_freq_min)
    target_idx_max = freq_to_idx(target_freq_max)
    percentage = 0.25

    for i in xrange(500):
        f = open('mock_data/input_%d.txt' % i, 'w')
        signal = stream.read(FRAME_SIZE)
        wave_data = np.fromstring(signal, dtype=np.int16) / 32768.
        f.write('\n'.join(map(str, wave_data)))
        f.close()

        freq_data = abs(np.fft.fft(wave_data))
        rank_data = np.searchsorted(np.sort(freq_data), freq_data)
        idx = rank_data[target_idx_min:target_idx_max+1].argmax()
        idx += target_idx_min

        f = open('mock_data/output_%d.txt' % i, 'w')
        if rank_data[idx] + 1 >= len(freq_data) * (1.0 - percentage):
            f.write('%8.15f' % idx_to_freq(idx))
        else:
            f.write('-1.0')
        f.close()
        print i


 
if __name__ == '__main__':
    main()
