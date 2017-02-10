package main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File; 
import java.io.IOException;

import javax.swing.*;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public class GUI implements ActionListener {
	JFileChooser chooser = new JFileChooser();// 文件选择器
	
	// 设置组件
	JFrame frame = new JFrame("XML-eBook");
    JPanel panel = new JPanel();  //new GridLayout(4, 2, 10, 10)
    JLabel textLabel = new JLabel("设置文本文件夹路径:");
    JLabel picLabel = new JLabel("设置图片文件夹路径:");
    JLabel audioLabel = new JLabel("设置音频文件夹路径:");
    JLabel videoLabel = new JLabel("设置视频文件夹路径:");
    JLabel outputLabel = new JLabel("设置导出文件夹路径:");
    JTextField textPath = new JTextField("/home/zhufree/workspace/eBookGenerator/src/static/text");
    JTextField picPath = new JTextField("/home/zhufree/workspace/eBookGenerator/src/static/pics");
    JTextField audioPath = new JTextField("/home/zhufree/workspace/eBookGenerator/src/static/sounds");
    JTextField videoPath = new JTextField("/home/zhufree/workspace/eBookGenerator/src/static/videos");
    JTextField outputPath = new JTextField("/home/zhufree/workspace/eBookGenerator/src/output");
    JButton textBtn = new JButton("选择文本文件夹");
    JButton picBtn = new JButton("选择图片文件夹");
    JButton audioBtn = new JButton("选择音频文件夹");
    JButton videoBtn = new JButton("选择视频文件夹");
    JButton outputBtn = new JButton("选择导出文件夹");
    JButton toHtmlBtn = new JButton("导出HTML");
    JButton toPdfBtn = new JButton("导出PDF");
	
	public GUI() {
        // 设置坐标位置和尺寸
        frame.setSize(550, 350);
        panel.setLayout(null);
        // 每个Label高50，宽150，上下边距0，左边距20，总宽200
        textLabel.setBounds(20, 0, 150, 50);
        picLabel.setBounds(20, 50, 150, 50);
        audioLabel.setBounds(20, 100, 150, 50);
        videoLabel.setBounds(20, 150, 150, 50);
        outputLabel.setBounds(20, 200, 150, 50);
        // 每个TextField高30，宽150（待调整），左边距200
        textPath.setBounds(160, 10, 200, 30);
        picPath.setBounds(160, 60, 200, 30);
        audioPath.setBounds(160, 110, 200, 30);
        videoPath.setBounds(160, 160, 200, 30);
        outputPath.setBounds(160, 210, 200, 30);
        // 每个btn 高30，宽130
        textBtn.setBounds(400, 10, 130, 30);
        picBtn.setBounds(400, 60, 130, 30);
        audioBtn.setBounds(400, 110, 130, 30);
        videoBtn.setBounds(400, 160, 130, 30);
        outputBtn.setBounds(400, 210, 130, 30);
        // 执行操作的btn
        toHtmlBtn.setBounds(50, 250, 200, 80);
        toPdfBtn.setBounds(300, 250, 200, 80);
        
        // 给按钮添加打开文件系统的监听器
        textBtn.addActionListener(this);
        picBtn.addActionListener(this);
        audioBtn.addActionListener(this);
        videoBtn.addActionListener(this);
        outputBtn.addActionListener(this);
        toHtmlBtn.addActionListener(this);
        toPdfBtn.addActionListener(this);
       
        // 添加组件
        // 这行为了本地调试方便，之后删除
        chooser.setCurrentDirectory(new File("/home/zhufree/workspace/eBookGenerator/src/static/"));
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel.add(textLabel);
        panel.add(textPath);
        panel.add(textBtn);
        panel.add(picLabel);
        panel.add(picPath);
        panel.add(picBtn);
        panel.add(audioLabel);
        panel.add(audioPath);
        panel.add(audioBtn);
        panel.add(videoLabel);
        panel.add(videoPath);
        panel.add(videoBtn);
        panel.add(outputLabel);
        panel.add(outputPath);
        panel.add(outputBtn);
        panel.add(toHtmlBtn);
        panel.add(toPdfBtn);
        frame.add(panel);
        frame.setVisible(true);
	}
	
	
	public void actionPerformed(ActionEvent e) {  
        String source = e.getActionCommand(); 
        chooser.setFileSelectionMode(1);// 设定只能选择到文件夹  
        int state;// 此句是打开文件选择器界面的触发语句  
        if (source.equals("选择文本文件夹")) {  
        	state = chooser.showOpenDialog(null);// 此句是打开文件选择器界面的触发语句  
        	if (state == 1) {  
                return;  
            } else {  
                File f = chooser.getSelectedFile();// f为选择到的目录  
                textPath.setText(f.getAbsolutePath());  
            }   
        }
        if (source.equals("选择图片文件夹")) {  
        	state = chooser.showOpenDialog(null);// 此句是打开文件选择器界面的触发语句  
        	if (state == 1) {  
                return;  
            } else {  
                File f = chooser.getSelectedFile();// f为选择到的目录  
                picPath.setText(f.getAbsolutePath());  
            }   
        } 
        if (source.equals("选择音频文件夹")) {  
        	state = chooser.showOpenDialog(null);
        	if (state == 1) {  
                return;  
            } else {  
                File f = chooser.getSelectedFile();// f为选择到的目录  
                audioPath.setText(f.getAbsolutePath());  
            }   
        } 
        if (source.equals("选择视频文件夹")) {  
        	state = chooser.showOpenDialog(null);
        	if (state == 1) {  
                return;  
            } else {  
                File f = chooser.getSelectedFile();// f为选择到的目录  
                videoPath.setText(f.getAbsolutePath());  
            }   
        } 
        if (source.equals("选择导出文件夹")) {  
        	state = chooser.showOpenDialog(null);
        	if (state == 1) {  
                return;  
            } else {  
                File f = chooser.getSelectedFile();// f为选择到的目录  
                outputPath.setText(f.getAbsolutePath());  
            }   
        } 
        // 排版操作
        if (source.equals("导出HTML")) {  
        	try {
				new ToHtml(textPath.getText(), 
						picPath.getText(), 
						audioPath.getText(), 
						videoPath.getText(), 
						outputPath.getText());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	return;
        }
        if (source.equals("导出PDF")) {  
        	try {
				new ToPdf(textPath.getText(), 
						picPath.getText(), 
						audioPath.getText(), 
						videoPath.getText(), 
						outputPath.getText());
			} catch (IOException | SAXException | TransformerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	return;
        }
    }  
	
	public static void main(String[] args) {
		new GUI();
	}
}
