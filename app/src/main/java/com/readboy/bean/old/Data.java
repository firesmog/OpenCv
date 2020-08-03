package com.readboy.bean.old;

import java.util.Arrays;

public class Data {
    private Block[] block;

    public Block[] getBlock() {
        return block;
    }

    public void setBlock(Block[] block) {
        this.block = block;
    }

    @Override
    public String toString() {
        return "Data{" +
                "block=" + Arrays.toString(block) +
                '}';
    }
}
