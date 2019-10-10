import java.util.ArrayList;

public class ARPLayer implements BaseLayer {

    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

    private static class ARPHeader {
        byte[] HWtype = new byte[2];
        byte[] protocol = new byte[2];
        byte HWLength;
        byte protLength;
        byte[] opcode = new byte[2];
        byte[] srcMac = new byte[6];
        byte[] srcIp = new byte[4];
        byte[] dstMac = new byte[6];
        byte[] dstIp = new byte[4];

        public ARPHeader() {
            HWtype[1] = (byte)0x01;

            protocol[0] = (byte) 0x80;

            HWLength = 6;
            protLength = 4;
        }
    }

    @Override
    public boolean Send(byte[] input, int length) {
        return false;
    }

    @Override
    public boolean Receive() {
        return false;
    }

    @Override
    public String GetLayerName() {
        return pLayerName;
    }

    @Override
    public BaseLayer GetUnderLayer() {
        if (p_UnderLayer == null)
            return null;
        return p_UnderLayer;
    }

    @Override
    public BaseLayer GetUpperLayer(int nindex) {
        if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
            return null;
        return p_aUpperLayer.get(nindex);
    }

    @Override
    public void SetUnderLayer(BaseLayer pUnderLayer) {
        if (pUnderLayer == null)
            return;
        p_UnderLayer = pUnderLayer;
    }

    @Override
    public void SetUpperLayer(BaseLayer pUpperLayer) {
        if (pUpperLayer == null)
            return;
        this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
    }

    @Override
    public void SetUpperUnderLayer(BaseLayer pUULayer) {
        this.SetUpperLayer(pUULayer);
        pUULayer.SetUnderLayer(this);
    }

    public static class ARPCache {
        private static String interfaceName;
        private static byte[] ipAddress = new byte[4];
        private static byte[] macAddress = new byte[6];
        private static boolean status;

        public static String InterfaceName() {
            return interfaceName;
        }

        public static byte[] IpAddress() {
            return ipAddress;
        }

        public static byte[] MacAddress() {
            return macAddress;
        }

        public static boolean Status() {
            return status;
        }

        public static void setInterfaceName(String interfaceName) {
            ARPCash.interfaceName = interfaceName;
        }

        public static void setIpAddress(byte[] ipAddress) {
            ARPCash.ipAddress = ipAddress;
        }

        public static void setMacAddress(byte[] macAddress) {
            ARPCash.macAddress = macAddress;
        }

        public static void setStatus(boolean status) {
            ARPCash.status = status;
        }
    }
}
