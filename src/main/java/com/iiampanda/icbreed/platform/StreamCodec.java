package com.iiampanda.icbreed.platform;

public interface StreamCodec<B, T> {
    T decode(B buf);

    void encode(B buf, T value);

    interface Encoder<B, T> {
        void encode(T value, B buf);
    }

    interface Decoder<B, T> {
        T decode(B buf);
    }

    static <B, T> StreamCodec<B, T> unit(T value) {
        return new StreamCodec<>() {
            @Override
            public T decode(B buf) {
                return value;
            }

            @Override
            public void encode(B buf, T v) {
            }
        };
    }

    static <B, T> StreamCodec<B, T> ofMember(Encoder<B, T> encoder, Decoder<B, T> decoder) {
        return new StreamCodec<>() {
            @Override
            public T decode(B buf) {
                return decoder.decode(buf);
            }

            @Override
            public void encode(B buf, T value) {
                encoder.encode(value, buf);
            }
        };
    }
}
