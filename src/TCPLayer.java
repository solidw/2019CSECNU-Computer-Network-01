package arp;

import java.util.ArrayList;

public class TCPLayer implements BaseLayer {

    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

    _TCP_HEADER m_tHeader = new _TCP_HEADER();
    int HeaderSize = 20;

    private class _TCP_HEADER {
        byte[] srcPort;
        byte[] destPort;
        byte[] sequenceNumber;
        byte[] ackNumber;
        byte[] conditions;
        byte[] windowSize;
        byte[] checkSum;
        byte[] urgentPointer;

        public _TCP_HEADER() {
            this.srcPort = new byte[2];
            this.destPort = new byte[2];
            this.sequenceNumber = new byte[4];
            this.ackNumber = new byte[4];
            this.conditions = new byte[2];
            this.windowSize = new byte[2];
            this.checkSum = new byte[2];
            this.urgentPointer = new byte[2];
        }
    }

    public TCPLayer(String pName){
        pLayerName = pName;
        ResetHeader();

    }

    public void ResetHeader() {

    }


    void copyHeader(byte[] buffer){
        int beforeHeaderSize = 0;

        System.arraycopy(this.m_tHeader.srcPort, 0, buffer, beforeHeaderSize, this.m_tHeader.srcPort.length);
        beforeHeaderSize += this.m_tHeader.srcPort.length;

        System.arraycopy(this.m_tHeader.destPort, 0, buffer, beforeHeaderSize, this.m_tHeader.destPort.length);
        beforeHeaderSize += this.m_tHeader.destPort.length;

        System.arraycopy(this.m_tHeader.sequenceNumber, 0, buffer, beforeHeaderSize, this.m_tHeader.sequenceNumber.length);
        beforeHeaderSize += this.m_tHeader.sequenceNumber.length;

        System.arraycopy(this.m_tHeader.ackNumber, 0, buffer, beforeHeaderSize, this.m_tHeader.ackNumber.length);
        beforeHeaderSize += this.m_tHeader.ackNumber.length;

        System.arraycopy(this.m_tHeader.conditions, 0, buffer, beforeHeaderSize, this.m_tHeader.conditions.length);
        beforeHeaderSize += this.m_tHeader.conditions.length;

        System.arraycopy(this.m_tHeader.windowSize, 0, buffer, beforeHeaderSize, this.m_tHeader.windowSize.length);
        beforeHeaderSize += this.m_tHeader.windowSize.length;

        System.arraycopy(this.m_tHeader.checkSum, 0, buffer, beforeHeaderSize, this.m_tHeader.checkSum.length);
        beforeHeaderSize += this.m_tHeader.checkSum.length;

        System.arraycopy(this.m_tHeader.urgentPointer, 0, buffer, beforeHeaderSize, this.m_tHeader.urgentPointer.length);
        beforeHeaderSize += this.m_tHeader.urgentPointer.length;
    }

    public byte[] ObjToByte(byte[] input, int length){
        byte[] buffer = new byte[length + HeaderSize];

        copyHeader(buffer);

        for (int i = 0; i < length; i++){
            buffer[i + HeaderSize] = input[i];
        }

        return buffer;
    }


    public byte[] removeHeader(byte[] input) {
        int inputLength = input.length;
        byte[] buf = new byte[inputLength - HeaderSize];

        // mac 주소와 type 등에 관한 정보를 삭제한다.
        for (int i = HeaderSize; i < inputLength; i++) {
            buf[i - HeaderSize] = input[i];
        }
        // 삭제한 저보를 반환한다.
        return buf;
    }


    @Override
    public synchronized boolean Send(byte[] input, int length) {

        byte[] buffer = ObjToByte(input, input.length);
        boolean result = this.GetUnderLayer().Send(buffer, buffer.length);
        return result;
    }

    @Override
    public synchronized boolean Receive(byte[] input) {
        this.GetUpperLayer(0).Receive(removeHeader(input));
        return false;
    }

    @Override
    public String GetLayerName() {
        // TODO Auto-generated method stub
        return pLayerName;
    }

    @Override
    public BaseLayer GetUnderLayer() {
        // TODO Auto-generated method stub
        if (p_UnderLayer == null)
            return null;
        return p_UnderLayer;
    }

    @Override
    public BaseLayer GetUpperLayer(int nindex) {
        // TODO Auto-generated method stub
        if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
            return null;
        return p_aUpperLayer.get(nindex);
    }

    @Override
    public void SetUnderLayer(BaseLayer pUnderLayer) {
        // TODO Auto-generated method stub
        if (pUnderLayer == null)
            return;
        this.p_UnderLayer = pUnderLayer;
    }

    @Override
    public void SetUpperLayer(BaseLayer pUpperLayer) {
        // TODO Auto-generated method stub
        if (pUpperLayer == null)
            return;
        this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
        // nUpperLayerCount++;

    }

    @Override
    public void SetUpperUnderLayer(BaseLayer pUULayer) {
        this.SetUpperLayer(pUULayer);
        pUULayer.SetUnderLayer(this);
    }
}
