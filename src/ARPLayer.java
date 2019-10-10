import java.util.ArrayList;

public class ARPLayer implements BaseLayer {

    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

    @Override
    public String GetLayerName() {
        return null;
    }

    @Override
    public BaseLayer GetUnderLayer() {
        return null;
    }

    @Override
    public BaseLayer GetUpperLayer(int nindex) {
        return null;
    }

    @Override
    public void SetUnderLayer(BaseLayer pUnderLayer) {

    }

    @Override
    public void SetUpperLayer(BaseLayer pUpperLayer) {

    }

    @Override
    public void SetUpperUnderLayer(BaseLayer pUULayer) {

    }

    public static class ARPCash {
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
