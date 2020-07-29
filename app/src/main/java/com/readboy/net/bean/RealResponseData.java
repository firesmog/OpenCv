package com.readboy.net.bean;

public class RealResponseData {
    private Block block;

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    @Override
    public String toString() {
        return "RealResponseData{" +
                "block=" + block +
                '}';
    }
}
