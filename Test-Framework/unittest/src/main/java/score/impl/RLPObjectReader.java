/*
 * Copyright 2022 ICONLOOP Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package score.impl;

import score.Address;
import score.ObjectReader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class RLPObjectReader implements ObjectReader {

    private static class ListFrame {
        int endPos;
    }

    private final ByteBuffer bb;
    private final byte[] arr;
    private final ArrayList<ListFrame> frames = new ArrayList<>();
    private ListFrame topFrame;
    private int o;
    private int l;
    //
    private int level = 0;

    public RLPObjectReader(byte[] data) {
        this(ByteBuffer.wrap(data));
    }

    private RLPObjectReader(ByteBuffer bb) {
        this.bb = bb;
        this.arr = bb.array();
        this.topFrame = new ListFrame();
        this.frames.add(topFrame);
        this.topFrame.endPos = bb.limit();
    }

    private void readRLPString() {
        var b = peek();
        if (b >= 0xc0) {
            throw new IllegalStateException();
        }
        peekRLPString(b);
        bb.position(o + l);
    }

    private void peekRLPString(int b) {
        var p = bb.arrayOffset() + bb.position();
        if (b <= 0x7f) {
            o = bb.position();
            l = 1;
        } else if (b <= 0xb7) {
            o = 1 + bb.position();
            l = b - 0x80;
        } else if (b == 0xb8) {
            o = 2 + bb.position();
            l = arr[p + 1] & 0xff;
        } else if (b == 0xb9) {
            o = 3 + bb.position();
            l = ((arr[p + 1] & 0xff) << 8) |
                    (arr[p + 2] & 0xff);
        } else if (b == 0xba) {
            o = 4 + bb.position();
            l = ((arr[p + 1] & 0xff) << 16) |
                    ((arr[p + 2] & 0xff) << 8) |
                    (arr[p + 3] & 0xff);
        } else if (b == 0xbb) {
            o = 5 + bb.position();
            l = ((arr[p + 1] & 0xff) << 24) |
                    ((arr[p + 2] & 0xff) << 16) |
                    ((arr[p + 3] & 0xff) << 8) |
                    (arr[p + 4] & 0xff);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void peekRLPListHeader(int b) {
        var p = bb.arrayOffset() + bb.position();
        if (b <= 0xf7) {
            o = 1 + bb.position();
            l = b - 0xc0;
        } else if (b == 0xf8) {
            o = 2 + bb.position();
            l = arr[p + 1] & 0xff;
        } else if (b == 0xf9) {
            o = 3 + bb.position();
            l = ((arr[p + 1] & 0xff) << 8) |
                    (arr[p + 2] & 0xff);
        } else if (b == 0xfa) {
            o = 4 + bb.position();
            l = ((arr[p + 1] & 0xff) << 16) |
                    ((arr[p + 2] & 0xff) << 8) |
                    (arr[p + 3] & 0xff);
        } else if (b == 0xfb) {
            o = 5 + bb.position();
            l = ((arr[p + 1] & 0xff) << 24) |
                    ((arr[p + 2] & 0xff) << 16) |
                    ((arr[p + 3] & 0xff) << 8) |
                    (arr[p + 4] & 0xff);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private boolean peekRLPNull(int b) {
        if (b != 0xf8) {
            return false;
        }
        var p = bb.arrayOffset() + bb.position();
        if (arr[p + 1] == 0) {
            o = 2 + bb.position();
            l = 0;
            return true;
        }
        return false;
    }

    private int peek() {
        return (bb.get(bb.position()) & 0xff);
    }

    public boolean readBoolean() {
        return readBigInteger().signum() != 0;
    }

    public byte readByte() {
        return readBigInteger().byteValue();
    }

    public short readShort() {
        return readBigInteger().shortValue();
    }

    public char readChar() {
        return (char) readBigInteger().intValue();
    }

    public int readInt() {
        return readBigInteger().intValue();
    }

    public float readFloat() {
        readRLPString();
        if (l != 4) {
            throw new IllegalStateException();
        }
        return bb.getFloat(o);
    }

    public long readLong() {
        return readBigInteger().longValue();
    }

    public double readDouble() {
        readRLPString();
        if (l != 8) {
            throw new IllegalStateException();
        }
        return bb.getDouble(o);
    }

    public BigInteger readBigInteger() {
        readRLPString();
        var offset = bb.arrayOffset() + o;
        return new BigInteger(arr, offset, l);
    }

    public String readString() {
        readRLPString();
        var offset = bb.arrayOffset() + o;
        return new String(arr, offset, l, StandardCharsets.UTF_8);
    }

    public byte[] readByteArray() {
        readRLPString();
        var offset = bb.arrayOffset() + o;
        return Arrays.copyOfRange(arr, offset, offset + l);
    }

    public Address readAddress() {
        byte[] b = readByteArray();
        if (b.length != Address.LENGTH) {
            throw new IllegalStateException();
        }
        return new Address(b);
    }

    @SuppressWarnings("unchecked")
    public <T> T read(Class<T> c) {
        if (c == java.lang.Boolean.class) {
            return (T) Boolean.valueOf(readBoolean());
        } else if (c == java.lang.Byte.class) {
            return (T) Byte.valueOf(readByte());
        } else if (c == java.lang.Short.class) {
            return (T) Short.valueOf(readShort());
        } else if (c == java.lang.Character.class) {
            return (T) Character.valueOf(readChar());
        } else if (c == java.lang.Integer.class) {
            return (T) Integer.valueOf(readInt());
        } else if (c == java.lang.Float.class) {
            return (T) Float.valueOf(readFloat());
        } else if (c == java.lang.Long.class) {
            return (T) Long.valueOf(readLong());
        } else if (c == java.lang.Double.class) {
            return (T) Double.valueOf(readDouble());
        } else if (c == java.lang.String.class) {
            return (T) readString();
        } else if (c == java.math.BigInteger.class) {
            return (T) readBigInteger();
        } else if (c == byte[].class) {
            return (T) readByteArray();
        } else if (c == Address.class) {
            return (T) readAddress();
        } else {
            try {
                var m = c.getDeclaredMethod("readObject", ObjectReader.class);
                if ((m.getModifiers()& Modifier.STATIC) == 0
                        || (m.getModifiers()&Modifier.PUBLIC) == 0) {
                    throw new IllegalArgumentException();
                }
                var res = m.invoke(null, this);
                return (T) res;
            } catch (NoSuchMethodException
                    | IllegalAccessException
                    | InvocationTargetException e) {
                e.printStackTrace();
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public <T> T readOrDefault(Class<T> c, T def) {
        if (!hasNext()) {
            return def;
        }
        return read(c);
    }

    @Override
    public <T> T readNullable(Class<T> c) {
        if (readNullity()) {
            return null;
        }
        return read(c);
    }

    @Override
    public <T> T readNullableOrDefault(Class<T> c, T def) {
        if (!hasNext()) {
            return def;
        }
        return readNullable(c);
    }

    @Override
    public void beginList() {
        ++level;
        readListHeader();
    }

    @Override
    public boolean beginNullableList() {
        if (readNullity()) {
            return false;
        }
        ++level;
        readListHeader();
        return true;
    }

    @Override
    public void beginMap() {
        ++level;
        readMapHeader();
    }

    @Override
    public boolean beginNullableMap() {
        if (readNullity()) {
            return false;
        }
        ++level;
        readMapHeader();
        return true;
    }

    @Override
    public void end() {
        if (level == 0) {
            throw new IllegalStateException();
        }
        while (hasNext()) {
            skip(1);
        }
        readFooter();
        --level;
    }

    @Override
    public void skip() {
        skip(1);
    }

    public boolean readNullity() {
        return this.tryReadNull();
    }

    @Override
    public void skip(int count) {
        for (int i = 0; i < count; i++) {
            var b = peek();
            if (!peekRLPNull(b)) {
                if (b < 0xc0) {
                    peekRLPString(b);
                } else {
                    peekRLPListHeader(b);
                }
            }
            bb.position(o + l);
        }
    }

    private void _readRLPListHeader() {
        var b = peek();
        peekRLPListHeader(b);
        bb.position(o);
        topFrame = new ListFrame();
        topFrame.endPos = bb.position() + l;
        frames.add(topFrame);
    }

    private void _readRLPListFooter() {
        frames.remove(frames.size() - 1);
        topFrame = frames.get(frames.size() - 1);
    }

    public void readListHeader() {
        _readRLPListHeader();
    }

    public void readMapHeader() {
        readListHeader();
    }

    public boolean hasNext() {
        return bb.position() < topFrame.endPos;
    }

    public void readFooter() {
        _readRLPListFooter();
    }

    private boolean tryReadNull() {
        var b = peek();
        if (!peekRLPNull(b)) {
            return false;
        }
        bb.position(o + l);
        return true;
    }

    public long getTotalReadBytes() {
        return bb.position();
    }
}
