package arp;
import java.awt.Color;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;

import java.awt.event.ActionEvent;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import javax.swing.JTable;

public class AppLayer extends JFrame implements BaseLayer{

	private JPanel contentPane, dialogPane, errorPane;
	private JTextField ipAddressTextField;
	private JTextField gratuitiousArpTextField;
	private JTextField ipAddressDlgTF;
	private JTextField ethernetAddressDlgTF;

	JTable arpCacheTable, proxyArpTable;
	DefaultTableModel arpCacheTableModel, proxyArpTableModel;
	JScrollPane arpCacheScrollPane, proxyArpScrollPane;
	DefaultTableCellRenderer arpTableRenderer = new DefaultTableCellRenderer();
	DefaultTableCellRenderer proxyTableRenderer = new DefaultTableCellRenderer();
	TableColumnModel arpColumnModel, proxyColumnModel;

	JComboBox comboBoxDevice;

	String arpStatus, arpEthernetAddress;

	final String[] proxyArpTableHeader = { "Device", "IP 주소", "Ethernet 주소" };
	final String[] arpCacheTableHeader = { "Interface", "IP 주소", "Ethernet 주소", "Status" };
	String[][] proxyArpTableContents = new String[0][3];
	String[][] arpCacheTableContents = new String[0][4];



	// field for layer
	static TCPLayer tcpLayer;
	static IPLayer ipLayer;
	static ARPLayer arpLayer;
	static EthernetLayer ethernetLayer;
	static NILayer niLayer;

	public String pLayerName = null;
	public int nUpperLayerCount = 0;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();

	private static LayerManager m_LayerMgr = new LayerManager();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AppLayer appLayer = new AppLayer("GUI");
					appLayer.setVisible(true);
					m_LayerMgr.AddLayer(appLayer);

					tcpLayer = new TCPLayer("Tcp");
					m_LayerMgr.AddLayer(tcpLayer);

					ipLayer = new IPLayer("Ip");
					m_LayerMgr.AddLayer(ipLayer);

					arpLayer = new ARPLayer("Arp");
					m_LayerMgr.AddLayer(arpLayer);

					ethernetLayer = new EthernetLayer("Ethernet");
					m_LayerMgr.AddLayer(ethernetLayer);

					niLayer = new NILayer("NI");
					m_LayerMgr.AddLayer(niLayer);

					m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *Arp ( *Ip ( *Tcp ) ( *GUI ) ) ) ) ");

					arpLayer.setAppLayer();
					ipLayer.setSrcIP(InetAddress.getLocalHost().getAddress());
					arpLayer.setSrcIp(InetAddress.getLocalHost().getAddress());

					InetAddress presentAddr = InetAddress.getLocalHost();
					NetworkInterface net = NetworkInterface.getByInetAddress(presentAddr);

					byte[] macAddressBytes = net.getHardwareAddress();
					arpLayer.setSrcMac(macAddressBytes);

					ethernetLayer.SetUpperLayer(ipLayer);

					// 어떤 어댑터를 사용할지 결정한다.
					// 디버깅을 통해 adapter list 를 이용하여 설정한다.
					niLayer.SetAdapterNumber(3);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	// create send using thread
	public class Send extends Thread{
		byte input[] = {0};

		public void setInput(byte[] input) {
			this.input = input;
		}

		public void run(){
			p_UnderLayer.Send(input, input.length);
		}
	}


	// 테이블에 arp를 추가한다.
	private void addArpToTable(ARPLayer.ARPCache arpCache){
		String ipAddress = ipByteToString(arpCache.getIpAddress());
		String macAddress = macToString(arpCache.getMacAddress());
		arpCacheTableModel.addRow(new String[]{
				arpCache.getInterfaceName(),
				ipAddress,
				macAddress});
	}


	// 추가할 arp를 테이블에서 확인하여 있다면 overrite한다.
	// 아니라면 table에 arp를 추가한다.
	public synchronized void addArpCacheToTable(ARPLayer.ARPCache arpCache){

		int rowCount = arpCacheTableModel.getRowCount();
		String storedIp, macAddress;
		String addIp = ipByteToString(arpCache.getIpAddress());
		for (int i = 0; i < rowCount; i++) {
			storedIp = (String)arpCacheTableModel.getValueAt(i, 1);
			if(storedIp.equals(addIp)){
				macAddress = macToString(arpCache.getMacAddress());
				arpCacheTableModel.setValueAt(macAddress, i, 2);
				return;
			}
		}

		addArpToTable(arpCache);
	}

	// 바이트 배열되 되어있는 맥주소를 문자열로 바꿔둔다.
	public String macToString(byte[] mac) {
		StringBuilder buf = new StringBuilder();

		// 바이트를 한개씩 읽어와서 문자열로 변환해준다.
		for (byte b : mac) {
			if (buf.length() != 0) {
				buf.append(':');
			}
			if (b >= 0 && b < 16) {
				buf.append('0');
			}

			buf.append(Integer.toHexString((b < 0) ? b + 255 : b).toUpperCase());
		}
		return buf.toString();
	}

	public String ipByteToString(byte[] bytes){
		String result = "";
		for(byte raw : bytes){
			result += raw & 0xFF;
			result += " ";
		}
		return result;
	}

	/**
	 * Create the frame.
	 */
	public AppLayer(String pName) {
		this.pLayerName = pName;
		setTitle("TestARP");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(500, 400, 700, 400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);

		// ARP Cache Panel------------------------------------------------------
		JPanel arpCachePanel = new JPanel();
		arpCachePanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "ARP Cache",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		arpCachePanel.setBounds(5, 5, 330, 313);
		contentPane.add(arpCachePanel);
		arpCachePanel.setLayout(null);

		JButton btnItemDelete = new JButton("Item Delete");
		btnItemDelete.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (arpCacheTable.getSelectedRow() >= 0) {
					arpCacheTableModel.removeRow(arpCacheTable.getSelectedRow());
				}
			}
		});
		btnItemDelete.setBounds(20, 230, 130, 30);
		arpCachePanel.add(btnItemDelete);

		JButton btnAllDelete = new JButton("All Delete");
		btnAllDelete.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < arpCacheTableModel.getRowCount(); i++) {
					arpCacheTableModel.removeRow(i);
				}
			}
		});
		btnAllDelete.setBounds(180, 230, 130, 30);
		arpCachePanel.add(btnAllDelete);

		JLabel ipAddressLbl = new JLabel("IP 주소");
		ipAddressLbl.setBounds(10, 272, 45, 30);
		arpCachePanel.add(ipAddressLbl);

		ipAddressTextField = new JTextField();
		ipAddressTextField.setBounds(57, 272, 190, 30);
		arpCachePanel.add(ipAddressTextField);
		ipAddressTextField.setColumns(10);

		JButton btnCacheSend = new JButton("Send");
		btnCacheSend.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String arpTemp[] = { ipAddressTextField.getText(), arpEthernetAddress, arpStatus };

				try {
					InetAddress destIp = InetAddress.getByName(arpTemp[0].trim());
					byte[] bytesIp = destIp.getAddress();

					// 입력된 값을 토대로 목적지 IP를 설정한다.
					ipLayer.setDestIP(bytesIp);
					arpLayer.setDstIp(bytesIp);

					// send를 시작한다.
					Send send = new Send();
					send.run();

				} catch (Exception e1) {
					e1.printStackTrace();
				}


				ipAddressTextField.setText("");
			}
		});

		btnCacheSend.setBounds(255, 272, 65, 30);
		arpCachePanel.add(btnCacheSend);

		arpCacheTableModel = new DefaultTableModel(arpCacheTableContents, arpCacheTableHeader);
		arpCacheTable = new JTable(arpCacheTableModel);
		arpCacheTable.setShowHorizontalLines(false);
		arpCacheScrollPane = new JScrollPane(arpCacheTable);
		arpCacheScrollPane.setBounds(10, 20, 310, 203);
		arpCacheTable.setShowGrid(false);
		arpTableRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		arpColumnModel = arpCacheTable.getColumnModel();
		for (int i = 0; i < arpColumnModel.getColumnCount(); i++)
			arpColumnModel.getColumn(i).setCellRenderer(arpTableRenderer);
		arpCachePanel.add(arpCacheScrollPane);

		// Proxy ARP Panel------------------------------------------------------
		JPanel proxyArpPanel = new JPanel();
		proxyArpPanel.setLayout(null);
		proxyArpPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Proxy ARP Entry",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		proxyArpPanel.setBounds(347, 5, 330, 240);
		contentPane.add(proxyArpPanel);

		proxyArpTableModel = new DefaultTableModel(proxyArpTableContents, proxyArpTableHeader);
		proxyArpTable = new JTable(proxyArpTableModel);
		proxyArpScrollPane = new JScrollPane(proxyArpTable);
		proxyArpScrollPane.setBounds(10, 20, 310, 165);
		proxyArpTable.setShowGrid(false);
		proxyTableRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		proxyColumnModel = proxyArpTable.getColumnModel();
		for (int i = 0; i < proxyColumnModel.getColumnCount(); i++)
			proxyColumnModel.getColumn(i).setCellRenderer(proxyTableRenderer);
		proxyArpPanel.add(proxyArpScrollPane);

		JButton btnProxyArpAdd = new JButton("Add");
		btnProxyArpAdd.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AddDialog addDialog = new AddDialog();
				addDialog.setVisible(true);
			}
		});
		btnProxyArpAdd.setBounds(20, 195, 130, 30);
		proxyArpPanel.add(btnProxyArpAdd);

		JButton btnProxyArpDelete = new JButton("Delete");
		btnProxyArpDelete.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (proxyArpTable.getSelectedRow() >= 0) {
					proxyArpTableModel.removeRow(proxyArpTable.getSelectedRow());
				}
			}
		});
		btnProxyArpDelete.setBounds(180, 195, 130, 30);
		proxyArpPanel.add(btnProxyArpDelete);

		// Gratuitious ARP Panel----------------------------------------------
		JPanel gratuitiousArpPanel = new JPanel();
		gratuitiousArpPanel.setLayout(null);
		gratuitiousArpPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Gratuitious ARP",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		gratuitiousArpPanel.setBounds(347, 255, 330, 63);
		contentPane.add(gratuitiousArpPanel);

		JLabel gratuitiousArpLbl = new JLabel("H/W 주소");
		gratuitiousArpLbl.setBounds(10, 20, 55, 30);
		gratuitiousArpPanel.add(gratuitiousArpLbl);

		gratuitiousArpTextField = new JTextField();
		gratuitiousArpTextField.setBounds(71, 20, 180, 30);
		gratuitiousArpPanel.add(gratuitiousArpTextField);
		gratuitiousArpTextField.setColumns(10);

		JButton btnGratuitiousSend = new JButton("Send");
		btnGratuitiousSend.setBounds(257, 20, 65, 30);
		btnGratuitiousSend.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String macStr = gratuitiousArpTextField.getText();
//				ethernetLayer.setMacAddr(macStr);
			}
		});
		gratuitiousArpPanel.add(btnGratuitiousSend);

		// Program panel------------------------------------------------------
		JButton btnProgramEnd = new JButton("종료");
		btnProgramEnd.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		btnProgramEnd.setBounds(245, 324, 90, 27);
		contentPane.add(btnProgramEnd);

		JButton btnCancel = new JButton("취소");
		btnCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnCancel.setBounds(347, 324, 90, 27);
		contentPane.add(btnCancel);
	}

	public class errorDialog extends JDialog {

		public errorDialog(String log) {
			setTitle("Error");
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setBounds(600, 500, 250, 110);
			errorPane = new JPanel();
			errorPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(errorPane);
			errorPane.setLayout(null);

			JLabel messege = new JLabel(log);
			messege.setBounds(40, 10, 200, 15);
			errorPane.add(messege);

			JButton btnOk = new JButton("OK");
			btnOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			btnOk.setBounds(85, 35, 60, 25);
			errorPane.add(btnOk);

		}
	}

	public class AddDialog extends JFrame {
		String[] deviceList = { "Interface 0", "Interface 1" };

		public AddDialog() {
			setTitle("Proxy ARP 추가");
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setBounds(550, 450, 300, 210);
			dialogPane = new JPanel();
			dialogPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(dialogPane);
			dialogPane.setLayout(null);

			JLabel lblDevice = new JLabel("Device");
			lblDevice.setBounds(50, 20, 40, 15);
			dialogPane.add(lblDevice);

			JLabel lblIpAddress = new JLabel("IP 주소");
			lblIpAddress.setBounds(50, 60, 40, 15);
			dialogPane.add(lblIpAddress);

			JLabel lblEthernetAddress = new JLabel("Ethernet 주소");
			lblEthernetAddress.setBounds(12, 100, 78, 15);
			dialogPane.add(lblEthernetAddress);

			comboBoxDevice = new JComboBox<String>(deviceList);
			comboBoxDevice.setBounds(110, 20, 150, 21);
			dialogPane.add(comboBoxDevice);

			ipAddressDlgTF = new JTextField();
			ipAddressDlgTF.setBounds(110, 60, 150, 21);
			dialogPane.add(ipAddressDlgTF);
			ipAddressDlgTF.setColumns(10);

			ethernetAddressDlgTF = new JTextField();
			ethernetAddressDlgTF.setColumns(10);
			ethernetAddressDlgTF.setBounds(110, 100, 150, 21);
			dialogPane.add(ethernetAddressDlgTF);

			JButton btnOk = new JButton("OK");
			btnOk.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String temp[] = { (String) comboBoxDevice.getSelectedItem(), ipAddressDlgTF.getText(),
							ethernetAddressDlgTF.getText() };

					String[] ipAddress = temp[1].split(".");
					/*
					if (ipAddress.length != 4) {
						errorDialog errorLog = new errorDialog("올바른 주소값이 아닙니다.");
						errorLog.setVisible(true);						
					} else if( ){
						
					} else {
						
					}
						proxyArpTableModel.addRow(temp);
						dispose();
					}*/
				}
			});
			btnOk.setBounds(41, 140, 97, 23);
			dialogPane.add(btnOk);

			JButton btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new java.awt.event.ActionListener() {

				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});btnCancel.setBounds(150,140,97,23);dialogPane.add(btnCancel);
		}
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
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}
}
