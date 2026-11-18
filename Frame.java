public class Frame {
    public int seqNum;
    public byte[] data;
    public long checksum;
    public boolean isAck;

    public Frame(int seqNum, byte[] data, long checksum, boolean isAck)
    {

    }

    public byte[] toBytes()
    {
        return null;
    }

    public static Frame fromBytes(byte[] bytes)
    {
        return null;
    }
}