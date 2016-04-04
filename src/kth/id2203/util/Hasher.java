package kth.id2203.util;


import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.io.*;

public class Hasher {
    public static int hash(int key, int range) {
        HashFunction hf = Hashing.murmur3_128();
        HashCode hc = hf.newHasher().putInt(key).hash();

        return hc.asInt() % range;
    }
}
