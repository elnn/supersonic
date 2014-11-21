
class Automata(object):

    def __init__(self):
        self.NOP = '!#'
        self.buf = self.NOP
        self.res = ''

    def feed(self, c):
        if c == self.buf[-1] == '$':
            if self.buf[-2] != '$' and self.buf[-2] != '^' and self.buf[-2] == self.buf[-3]:
                self.res += self.buf[-2]
                print 's = %s' % self.res
            else:
                self.buf = self.NOP
        elif c == self.buf[-1] == '^':
            self.res = ''
        self.buf += c


def test():
    automata = Automata()
    signal = '044444444$$$$$$$8888888$$$$$$96666666$$$$$$$'
    for c in signal:
        automata.feed(c)
    print automata.res

if __name__ == '__main__':
    test()
