package main;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ToHtml {
	public final String imgKeyword = "pic", soundKeyword = "sound", videoKeyword = "video";
	String textPath, picPath, audioPath, videoPath, outputPath;
	int lcount, rcount;

	public ToHtml(String textPath, String picPath, String audioPath, String videoPath, 
			String outputPath) throws IOException {
		this.textPath = textPath;
		this.picPath = picPath;
		this.audioPath = audioPath;
		this.videoPath = videoPath;
		this.outputPath = outputPath;
		this.lcount = 0;
		this.rcount = 0;
		File textDir = new File(textPath);//读取文本文件夹
		if(textDir.isDirectory()){
			File[] textFiles = textDir.listFiles();//遍历文本文件，依次执行处理
			for(File textFile: textFiles){
				if(!textFile.getName().endsWith("~")){
					handleHtml(textFile);
				}
			}
		}
	}
	
	public void changeImgSize(BufferedImage rawImg, String newPicUrl) throws IOException{
		//处理需要缩小的图片
		System.out.println("==========修改图片尺寸==========");
		//获取原始图片长宽
	    int rawWidth = rawImg.getWidth(), rawHeight = rawImg.getHeight();
	    //计算缩小的比例
	    float radio = (float) rawWidth/300;
	    //计算缩小后的长宽
	    int newWidth = 300, newHeight = (int) (rawHeight/radio);
	    Image smallImg = rawImg.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);//缩小
	    BufferedImage newImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = newImg.getGraphics();
        g.drawImage(smallImg, 0, 0, null); // 绘制缩小后的图
        g.dispose();
        ImageIO.write(newImg, "JPEG", new File(newPicUrl));// 输出到文件流
	}
	
	public String insertImg(String imgText, String imgId, int lcount, int rcount) throws IOException{
		//插入图片函数
		String fullPicName = this.imgKeyword + imgId;//获取不带后缀的文件全名
		System.out.println("==========插入图片： " + fullPicName + "==========");
		File imgDir = new File(picPath);
		String newDiv = "";
		if(imgDir.isDirectory()){
			File[] imgFiles = imgDir.listFiles();//遍历图片文件
			for(File imgFile: imgFiles){
				if(imgFile.getName().startsWith(fullPicName)){//按文件名匹配图片
					BufferedImage curImg = ImageIO.read(imgFile);//打开图片文件
					if(curImg.getWidth() > 400 && curImg.getWidth() > curImg.getHeight()){
						newDiv = "<div class='pic_in_text_center'><img src='../static/pics/" 
								+ imgFile.getName() + "' alt=''></div>";
					}else if(curImg.getWidth() > 400 && curImg.getWidth() < curImg.getHeight()){
						//改图片大小
						this.changeImgSize(curImg, "src/static/pics/small_" + imgFile.getName());
						if(lcount > rcount){
							newDiv = "<div class='pic_in_text_right'><img src='../static/pics/small_" 
									+ imgFile.getName() + "' alt=''></div>";
							this.rcount += 1;
						}else{
							newDiv = "<div class='pic_in_text_left'><img src='../static/pics/small_" 
									+ imgFile.getName() + "' alt=''></div>";
							this.lcount += 1;
						}
					}else{
						if(lcount > rcount){
							newDiv = "<div class='pic_in_text_right'><img src='../static/pics/" 
									+ imgFile.getName() + "' alt=''></div>";
							this.rcount += 1;
						}else{
							newDiv = "<div class='pic_in_text_left'><img src='../static/pics/" 
									+ imgFile.getName() + "' alt=''></div>";
							this.lcount += 1;
						}
					}
				}
			}
		}
		return newDiv;
	}
	
	public String insertSound(String soundText, String soundId){
		//读取音频文件，生成一个音频播放按钮
		String fullSoundName = this.soundKeyword + soundId;
		System.out.println("==========插入音频 " + fullSoundName + "==========");
		File soundDir = new File(audioPath);
		String newPalyLogo = "";
		if(soundDir.isDirectory()){
			File[] soundFiles = soundDir.listFiles();//遍历音频文件
			for(File soundFile: soundFiles){
				//System.out.println(file.getName());
				if(soundFile.getName().startsWith(fullSoundName)){//按文件名匹配音频
					String onclick = "play_sound('../static/sounds/" + soundFile.getName() + "')";
					newPalyLogo = "<img class='play_logo' onclick=" + onclick + " src='../static/img/play.png'/>";
				}
			}
		}
		return newPalyLogo;
	}
	
	public String insertVideo(String videoText, String videoId) throws IOException{
		//读取视频文件，生成一个视频页面，并返回打开链接的link
		String fullVideoName = this.videoKeyword + videoId;
		System.out.println("==========插入视频 " + fullVideoName + "==========");
		File videoDir = new File(videoPath);
		String newVideoPage = "";
		String newVideoLink = "";
		if(videoDir.isDirectory()){
			File[] videoFiles = videoDir.listFiles();//遍历音频文件
			for(File videoFile: videoFiles){
				if(videoFile.getName().startsWith(fullVideoName)){//按文件名匹配音频
					newVideoPage = "<video class='video_in_text' controls='controls' height='500' style='clear: both;display: block;margin: auto;' src='../static/videos/" + videoFile.getName() + "' width='600'></video>";
					RandomAccessFile outputFile = new RandomAccessFile("src/output/" + videoFile.getName() + ".html", "rw");
					outputFile.seek(0);
					outputFile.write(newVideoPage.getBytes());
					outputFile.close();//输出保存html文件
					newVideoLink = "<a target='_blank' href='" + videoFile.getName() + ".html'>" + videoText + "</a>";
				}
			}
		}
		return newVideoLink;
	}
	
	public void handleHtml(File textFile) throws IOException{
		this.lcount = 0;
		this.rcount = 0;//清零判定图片插入位置的计数器
		String encoding = "UTF-8"; 
		File htmlInput = new File("src/rawhtml/base.html");
		Document baseHtml = Jsoup.parse(htmlInput, "UTF-8", "http://example.com/");//读取html模板文件
		InputStreamReader base_reader = new InputStreamReader(   
				new FileInputStream(textFile), encoding);   
		BufferedReader basebufferedReader = new BufferedReader(base_reader);   
		
		Element text_box = baseHtml.select("div#text").first();
	    Element body = baseHtml.select("body").first();
	    Element title = baseHtml.select("h3").first(); 
		
		String eachline = basebufferedReader.readLine();  
		title.text(eachline);//设置标题
		
		ArrayList<String> lines = new ArrayList<String>();  //实例化一个数组装文章段落
		while (eachline != null) {   
			if(eachline.length() > 0){
				lines.add(eachline);
			}
			eachline = basebufferedReader.readLine(); 
		} //读段落,存入数组	
		lines.remove(lines.get(0));//删除标题段落（默认为第一段）

		//设置正则匹配符
	    Pattern imgPat = Pattern.compile("\\((\\W+?)\\)\\[" + imgKeyword + "(\\S+?)\\]");
		Pattern soundPat = Pattern.compile("\\((\\W+?)\\)\\[" + soundKeyword + "(\\S+?)\\]");
		Pattern videoPat = Pattern.compile("\\((\\W+?)\\)\\[" + videoKeyword + "(\\S+?)\\]");
		//遍历段落进行匹配
		for(String line: lines){
			Matcher img_mat = imgPat.matcher(line);
			Matcher sound_mat = soundPat.matcher(line);
			Matcher video_mat = videoPat.matcher(line);
			String para = "<p>" + line.trim();
			//匹配图片
			if(img_mat.find()){
				String imgText = img_mat.group(1);
				String imgId = img_mat.group(2);
				// System.out.println(this.lcount + " " + this.rcount);
				//插入图片
				text_box.append(this.insertImg(imgText, imgId, this.lcount, this.rcount));
				//段落文字去掉匹配标示
				para = para.replace(img_mat.group(0), img_mat.group(1));
			}
			//匹配音频
			if(sound_mat.find()){
				String soundText = sound_mat.group(1);
				String soundId = sound_mat.group(2);
				//在段落结尾加上图片播放logo
				para += this.insertSound(soundText, soundId);
				para = para.replace(sound_mat.group(0), sound_mat.group(1));
			}
			//匹配视频
			if(video_mat.find()){
				String videoText = video_mat.group(1);
				String videoId = video_mat.group(2);
				//替换段落文字成视频链接
				para = para.replace(video_mat.group(0), this.insertVideo(videoText, videoId));
			}
			//加上结尾标识符，添加进文本块
			text_box.append(para + "</p><br>");
		}
		
		//加入音频播放器代码
		File audioFile = new File("src/rawhtml/audio.html");
		InputStreamReader audio_reader = new InputStreamReader(   
				new FileInputStream(audioFile), encoding);   
		BufferedReader audiobufferedReader = new BufferedReader(audio_reader);  
		String audioStr = "";
		eachline = audiobufferedReader.readLine();
		while(eachline!=null){
			audioStr += eachline + "\n";
			eachline = audiobufferedReader.readLine();
		}
		//读取所有段落拼接，加入网页body部分最后
		body.append(audioStr);
		audio_reader.close();
		
		//生成html文件
		FileWriter fileWritter = new FileWriter(outputPath + "/" + textFile.getName().substring(0, textFile.getName().length()-4) + ".html");
		BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
		bufferWritter.write(baseHtml.toString());
		bufferWritter.close();
		base_reader.close();  
	}
	public static void main(String[] args) throws IOException {
		new ToHtml("src/static/text", "src/static/pics", "src/static/sounds", "src/static/videos", "src/output");//实例化类，设定判定图片，音频，视频的关键词参数
		
	}
}
