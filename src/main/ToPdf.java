package main;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;
import org.xml.sax.SAXException;

public class ToPdf {
	public final String imgKeyword = "pic", soundKeyword = "sound", videoKeyword = "video";
	String textPath, picPath, audioPath, videoPath, outputPath;
	int lcount, rcount, linecount;

	public ToPdf(String textPath, String picPath, String audioPath, String videoPath, 
				String outputPath) throws IOException, SAXException, TransformerException  {
		this.textPath = textPath;
		this.picPath = picPath;
		this.audioPath = audioPath;
		this.videoPath = videoPath;
		this.outputPath = outputPath;
		this.lcount = 0;
		this.rcount = 0;//在左侧或右侧插入图片的计数器
		this.linecount= 0;//行数计数用来判断是否换页，如果换页使左右计数器都清零
		File textDir = new File(textPath);//读取文本文件夹
		if(textDir.isDirectory()){
			File[] textFiles = textDir.listFiles();//遍历文本文件，依次执行处理
			for(File textFile: textFiles){
				if(!textFile.getName().endsWith("~")){
					//处理文本文件转换成fo文件
					handle_pdf(textFile);
				}
			}
		}
		//设置转换配置文件
		FopFactory fopFactory = FopFactory.newInstance(new File("F:/code/libs/fop-2.1/conf/fop.xconf"));
		//设置输出文件
		
	    //设置源文件
		File foDir = new File(outputPath);
	    if(textDir.isDirectory()){
			File[] foFiles = foDir.listFiles();//遍历文本文件，依次执行处理
			for(File foFile: foFiles){
				if(foFile.getName().endsWith(".fo")){
					OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(outputPath + "/" + 
						foFile.getName().substring(0, foFile.getName().length()-3) + ".pdf")));
					Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
				    TransformerFactory factory = TransformerFactory.newInstance();
				    Transformer transformer = factory.newTransformer(); // identity transformer
					//处理文本文件转换成fo文件
					Source src = new StreamSource(foFile);
				    Result res = new SAXResult(fop.getDefaultHandler());
				    //转换
				    transformer.transform(src, res);
				    out.close();
				}
			}
		}
	}
	
	public void changeImgSize(BufferedImage rawImg, String newPicUrl) throws IOException{
		//处理需要缩小的图片
		System.out.println("改变图片大小：");
		//获取原始图片长宽
	    int rowWidth = rawImg.getWidth(), rowHeight = rawImg.getHeight();
	    //计算缩小的比例
	    float radio = (float) rowWidth/300;
	    //计算缩小后的长宽
	    int newWidth = 300, newHeight = (int) (rowHeight/radio);
	    
	    Image smallImg = rawImg.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);//缩小
	    BufferedImage newImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = newImg.getGraphics();
        g.drawImage(smallImg, 0, 0, null); // 绘制缩小后的图
        g.dispose();
        ImageIO.write(newImg, "JPEG", new File(newPicUrl));// 输出到文件流
	}
	
	public String insertImg(String imgText, String imgId) throws IOException{
		//插入图片函数
		String fullPicName = this.imgKeyword + imgId;//获取不带后缀的文件全名
		System.out.println("==========insert img " + fullPicName + "==========");
		File imgDir = new File(picPath);
		String newBlock = "";
		if(imgDir.isDirectory()){
			File[] imgFiles = imgDir.listFiles();//遍历图片文件
			for(File imgFile: imgFiles){
				if(imgFile.getName().startsWith(fullPicName)){//按文件名匹配图片
					BufferedImage curImg = ImageIO.read(imgFile);//打开图片文件
					if(curImg.getWidth() > 400 && curImg.getWidth() > curImg.getHeight()){
						newBlock += "\n<fo:external-graphic text-align=\"center\" " +
									"width='" + (curImg.getWidth()/50+0.5) + "cm' " +
									"content-width='" + curImg.getWidth()/50 + "cm' " +
									"height='" + (curImg.getHeight()/50+0.5) + "cm' " +
									"content-height='" + curImg.getHeight()/50 + "cm' " +
									"scaling=\"uniform\" " +
									"src=\"url('static/pics/" +
									imgFile.getName() + "')\"\n/>";//不需浮动，直接插入图片
					}else if(curImg.getWidth() > 400 && curImg.getWidth() < curImg.getHeight()){
						//改图片大小
						this.changeImgSize(curImg, "src/static/pics/small_" + imgFile.getName());
						if(this.lcount > this.rcount){
							newBlock += "\n<fo:float float=\"end\"> \n<fo:block>\n<fo:external-graphic text-align=\"right\" " +
									"width='" + (curImg.getWidth()/50+0.5) + "cm' " +
									"content-width='" + curImg.getWidth()/50 + "cm' " +
									"height='" + (curImg.getHeight()/50+0.5) + "cm' " +
									"content-height='" + curImg.getHeight()/50 + "cm' " +
									"scaling=\"uniform\" " +
									"src=\"url('static/pics/small_" +
									imgFile.getName() + "')\"/>" + "\n</fo:block>\n</fo:float>\n";//插入右侧浮动的图片
							System.out.println("right");
							this.rcount += 1;
						}else{
							newBlock += "\n<fo:float float=\"start\"> \n<fo:block>\n<fo:external-graphic text-align=\"left\" " +
									"width='" + (curImg.getWidth()/50+0.5) + "cm' " +
									"content-width='" + curImg.getWidth()/50 + "cm' " +
									"height='" + (curImg.getHeight()/50+0.5) + "cm' " +
									"content-height='" + curImg.getHeight()/50 + "cm' " +
									"scaling=\"uniform\" " +
									"src=\"url('static/pics/small_" +
									imgFile.getName() + "')\"/>" + "\n</fo:block>\n</fo:float>\n";//插入左侧浮动的图片
							this.lcount += 1;
						}
					}else{
						if(this.lcount > this.rcount){
							newBlock += "\n<fo:float float=\"end\"> \n<fo:block>\n<fo:external-graphic text-align=\"right\" " +
									"width='" + (curImg.getWidth()/50+0.5) + "cm' " +
									"content-width='" + curImg.getWidth()/50 + "cm' " +
									"height='" + (curImg.getHeight()/50+0.5) + "cm' " +
									"content-height='" + curImg.getHeight()/50 + "cm' " +
									"scaling=\"uniform\" " +
									"src=\"url('static/pics/" +
									imgFile.getName() + "')\"/>" + "\n</fo:block>\n</fo:float>\n";
							System.out.println("right");
							this.rcount += 1;
						}else{
							newBlock += "\n<fo:float float=\"start\"> \n<fo:block>\n<fo:external-graphic text-align=\"left\" " +
									"width='" + (curImg.getWidth()/50+0.5) + "cm' " +
									"content-width='" + curImg.getWidth()/50 + "cm' " +
									"height='" + (curImg.getHeight()/50+0.5) + "cm' " +
									"content-height='" + curImg.getHeight()/50 + "cm' " +
									"scaling=\"uniform\" " +
									"src=\"url('static/pics/" +
									imgFile.getName() + "')\"/>" + "\n</fo:block>\n</fo:float>\n";
							this.lcount += 1;
						}
					}
				}
			}
		}
		return newBlock;
	}
	
	public void handle_pdf(File textFile) throws IOException{
		this.lcount = 0;
		this.rcount = 0;//清零判定图片插入位置的计数器
		String encoding = "UTF-8"; 
		InputStreamReader base_reader = new InputStreamReader(   
				new FileInputStream(textFile), encoding);   
		BufferedReader basebufferedReader = new BufferedReader(base_reader);   
		
		String all_fo = " ";
		String rootHead = "<fo:root xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n";
		String layout_master_str = "<fo:layout-master-set>\n" +
									"<fo:simple-page-master master-name=\"simple\" " +
					                  "page-height=\"29cm\" " +
					                  "page-width=\"21cm\" " +
					                  "margin-top=\"1cm\" " +
					                  "margin-bottom=\"2cm\" " +
					                  "margin-left=\"2.5cm\" " +
					                  "margin-right=\"2.5cm\">\n" +
								      "<fo:region-body margin-top=\"1cm\" />\n" +
								      "<fo:region-before extent=\"3cm\"/>\n" +
								      "<fo:region-after extent=\"1.5cm\"/>\n" +
								    "</fo:simple-page-master>\n" +
								  "</fo:layout-master-set>\n";
		String flowHead = "<fo:page-sequence master-reference=\"simple\">\n" +
							"<fo:flow flow-name=\"xsl-region-body\" border=\"0\" padding=\"0\">\n";
		String titleBlock = "<fo:block font-size=\"18pt\" " +
					            "font-family=\"micro\" " +
					            "line-height=\"2cm\" " +
					            "space-after.optimum=\"15pt\" " +
					            "background-color=\"blue\" " +
					            "color=\"white\" " +
					            "text-align=\"center\" " +
					            "padding-top=\"3pt\">\n";
		
		String eachline = basebufferedReader.readLine();  
		titleBlock += eachline + "</fo:block>\n";
		ArrayList<String> lines = new ArrayList<String>();  //实例化一个数组装文章段落
		while (eachline != null) {   
			if(eachline.length() > 0){
				lines.add(eachline);
			}
			eachline = basebufferedReader.readLine(); 
		} //读段落,存入数组	
		lines.remove(lines.get(0));//删除标题段落（默认为第一段）
		all_fo = rootHead + layout_master_str + flowHead + titleBlock;
		String lineBlockHead = "<fo:block font-size=\"12pt\" " +
				                "font-family=\"micro\" " +
				                "line-height=\"1cm\" " +
				                "space-after.optimum=\"3pt\" " +
				                "text-align=\"justify\">\n";
		//设置正则匹配符
	    Pattern imgPat = Pattern.compile("\\((\\W+?)\\)\\[" + imgKeyword + "(\\S+?)\\]");
	    Pattern soundPat = Pattern.compile("\\((\\W+?)\\)\\[" + soundKeyword + "(\\S+?)\\]");
		Pattern videoPat = Pattern.compile("\\((\\W+?)\\)\\[" + videoKeyword + "(\\S+?)\\]");
	    //遍历段落进行匹配
		for(String line: lines){
			this.linecount += line.length()/37+1;//判断这一段将生成多少行，并加入行数计数器中
//			System.out.println(this.linecount);
			if(this.linecount >= 22){//当行数超过页面最大行数
				this.lcount = 0;
				this.rcount = 0;
				this.linecount = 0;//三个计数器都清零，进入下一页
			}
			Matcher imgMat = imgPat.matcher(line);
			Matcher soundMat = soundPat.matcher(line);
			Matcher videoMat = videoPat.matcher(line);
			String lineBlock = lineBlockHead + line.trim();
			//匹配图片
			if(imgMat.find()){
				String imgText = imgMat.group(1);
				String imgId = imgMat.group(2);
				//插入图片
				//段落文字去掉匹配标示
				lineBlock += this.insertImg(imgText, imgId);
				lineBlock = lineBlock.replace(imgMat.group(0), imgMat.group(1));
			}
			if(soundMat.find()){
				//替换标示文字
				lineBlock = lineBlock.replace(soundMat.group(0), soundMat.group(1));
			}
			//匹配视频
			if(videoMat.find()){
				//替换标示文字
				lineBlock = lineBlock.replace(videoMat.group(0), videoMat.group(1));
			}
			//加上结尾标识符，添加进文本块
			 all_fo += lineBlock + "\n</fo:block>\n";
			 
		}
		all_fo += "</fo:flow>\n</fo:page-sequence>\n</fo:root>";
//		System.out.println(all_fo);
		//生成fo文件
		FileWriter fileWritter = new FileWriter(outputPath + "/" + textFile.getName().substring(0, textFile.getName().length()-4) + ".fo");
		BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		bufferWritter.write(all_fo);
		bufferWritter.close();
		base_reader.close();  
	}
	
	public static void main(String[] args) throws IOException, SAXException, TransformerException {
		new ToPdf("src/static/text", "src/static/pics", "src/static/sounds", "src/static/videos", "src/output");//实例化类，设定判定图片，音频，视频的关键词参数
    }
}
