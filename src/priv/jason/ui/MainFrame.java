package priv.jason.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import priv.jason.memcache.MemcachedUtil;
import priv.jason.memcache.RedisUtil;

public class MainFrame{
	
	public static final String[] SERVER = {"MEMCACHE", "REDIS"};
	public static final String[] MEMCACHE = {"192.168.12.12:10026", "192.168.12.12:10035"};
	public static final String[] REDIS = {"192.168.12.12:6379", "192.168.12.12:6380"};
	public static final String[] OPERATION1 = {"获取", "设置", "删除"};
	public static final String[] OPERATION2 = {"获取", "设置", "删除", "获取有序集合", "设置有序集合"};
	public static boolean isConnected = false;
	
	public MainFrame() {
		final JFrame frame = new JFrame("CacheClient");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(745, 560);
		frame.setLocationRelativeTo(null);
		frame.setLayout(null);
		
		JPanel panel1 = new JPanel(new FlowLayout());
		panel1.setBounds(10, 20, 700, 40);
		frame.add(panel1);
		JPanel panel2 = new DrawPanel();
		panel2.setBounds(10, 60, 700, 20);
		frame.add(panel2);
		final JPanel panel3 = new JPanel(new FlowLayout());
		panel3.setBounds(10, 80, 700, 40);
		frame.add(panel3);
		
		
		JLabel label1 = new JLabel("Cache类型:");
		panel1.add(label1);
		final JComboBox<String> comboBox1 = new JComboBox<String>(SERVER);
		panel1.add(comboBox1);
		
		JLabel label2 = new JLabel("服务器地址:");
		panel1.add(label2);
		final JComboBox<String> comboBox2 = new JComboBox<String>(MEMCACHE);
		panel1.add(comboBox2);
		
		final JButton button1 = new JButton("连接");
		panel1.add(button1);
		
		final JButton button2 = new JButton("断开");
		button2.setEnabled(false);
		panel1.add(button2);
		
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				connectServer(comboBox1.getSelectedIndex(), comboBox2.getSelectedItem().toString());
				JOptionPane.showMessageDialog(frame, "连接成功", "连接结果", JOptionPane.INFORMATION_MESSAGE);
				button1.setEnabled(false);
				button2.setEnabled(true);
				isConnected = true;
			}
		});
		
		button2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				disconnect(comboBox1.getSelectedIndex());
				JOptionPane.showMessageDialog(frame, "连接已断开", "断开连接", JOptionPane.INFORMATION_MESSAGE);
				button1.setEnabled(true);
				button2.setEnabled(false);
				isConnected = false;
				frame.validate();
			}
		});
		
		JLabel label3 = new JLabel("指令类型:");
		panel3.add(label3);
		final JComboBox<String> comboBox3 = new JComboBox<String>(OPERATION1);
		panel3.add(comboBox3);
		
		JLabel label4 = new JLabel("KEY:");
		panel3.add(label4);
		final JTextField textField = new JTextField();  
		textField.setColumns(15);  
		textField.setPreferredSize(new Dimension(100, 25));
		panel3.add(textField);  
		
		final JLabel label6 = new JLabel("VALUE:");
		final JTextField textField1 = new JTextField();  
		textField1.setColumns(15);  
		textField1.setPreferredSize(new Dimension(100, 25));
		
		final JButton button3 = new JButton("执行");
		panel3.add(button3);
		
		JPanel panel4 = new DrawPanel();
		panel4.setBounds(10, 120, 700, 20);
		frame.add(panel4);
		
		JPanel panel5 = new JPanel();
		panel5.setBounds(10, 140, 700, 400);
		panel5.setLayout(new FlowLayout());
		JLabel label5 = new JLabel("执行结果:");
		panel5.add(label5);
		
		final JTextArea textArea = new JTextArea(20, 40);
		textArea.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		panel5.add(scrollPane);
		frame.add(panel5);
		
		comboBox1.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				int state = e.getStateChange();
				if (state == 1) {
					int selectedIndex = comboBox1.getSelectedIndex();
					if (selectedIndex == 0) {
						comboBox2.removeAllItems();
						for (String str : MEMCACHE) {
							comboBox2.addItem(str);
						}
						comboBox3.removeAllItems();
						for (String str : OPERATION1) {
							comboBox3.addItem(str);
						}
					} else if (selectedIndex == 1) {
						comboBox2.removeAllItems();
						for (String str : REDIS) {
							comboBox2.addItem(str);
						}
						comboBox3.removeAllItems();
						for (String str : OPERATION2) {
							comboBox3.addItem(str);
						}
					}
				}
			}
		});
		
		button3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isConnected) {
					JOptionPane.showMessageDialog(frame, "尚未连接服务器，请先点击连接", "连接状态", JOptionPane.ERROR_MESSAGE);
					return;
				}
				int serverType = comboBox1.getSelectedIndex();
				int operateType = comboBox3.getSelectedIndex();
				String result = "";
				if (operateType == 1 || operateType == 4) {
					result = operate(serverType, operateType, textField.getText(), textField1.getText());
				} else {
					result = operate(serverType, operateType, textField.getText());
				}
				textArea.setText(result);
			}
		});
		
		comboBox3.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				int state = e.getStateChange();
				if (state == 1) {
					int selectedIndex = comboBox3.getSelectedIndex();
					if (selectedIndex == 1 || selectedIndex == 4) {//设置
						panel3.remove(button3);
						panel3.add(label6);
						panel3.add(textField1);  
						panel3.add(button3);
						panel3.revalidate();
					} else {
						panel3.remove(label6);
						panel3.remove(textField1);  
						panel3.revalidate();
					}
				} 
			}
		});
		
		frame.setVisible(true);
	}
	
	class DrawPanel extends JPanel {//画线的JPanel
		private static final long serialVersionUID = 3779494759815157859L;

		@Override
	    public void paint(Graphics g) {
	        super.paint(g);
	        Graphics2D g2 = (Graphics2D)g;
	        g2.setStroke(new BasicStroke(3.0f));//粗细
	        g2.setColor(Color.GRAY);//颜色
	        g2.drawLine(10, 10, 780, 10);//起始点
	    }
	}
	
	public static void main(String[] args) throws InterruptedException {
		new MainFrame();
		
	}
	
	public void connectServer(int serverType, String address) {
		if (serverType == 0) {//MEMCACHE
			MemcachedUtil.connect(address);
		} else if (serverType == 1) {
			RedisUtil.connect(address);
		}
	}
	
	public void disconnect(int serverType) {
		if (serverType == 0) {
			MemcachedUtil.disconnect();
		} else if (serverType == 0) {
			RedisUtil.disconnect();
		}
	}
	
	public static String operate(int serverType, int operateType, String... param) {
		String result = "[SUCCESS]";
		try {
			if (serverType == 0) {//MEMCACHE
				switch (operateType) {
				case 0://读取
					Object ret = MemcachedUtil.get(param[0]);
					if (ret == null) {
						result = "null";
					} else {
						result = ret.toString();
					}
					break;
				case 1://设置
					MemcachedUtil.set(param[0], param[1]);
					break;
				case 2:
					MemcachedUtil.delete(param[0]);
					break;
				default:
					break;
				}
			} else if (serverType == 1) {//REDIS
				switch (operateType) {
				case 0:
					Object ret = RedisUtil.getJedisInstance().get(param[0]);
					if (ret == null) {
						result = "null";
					} else {
						result = ret.toString();
					}
					break;
				case 1:
					RedisUtil.getJedisInstance().set(param[0], param[1]);
					break;
				case 2:
					RedisUtil.getJedisInstance().del(param[0]);
					break;
				case 3:
					result = RedisUtil.zrevrangeWithScores(param[0]);
					break;
				case 4:
					RedisUtil.zincrbyWithTime(param[0], param[1]);
					break;
				default:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = "[FAIL]\n" + e.getMessage();
		}
		return result;
	}
	
}
