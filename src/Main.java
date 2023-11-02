import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Main {

    private static final String IN_FILE_TXT = "inFile.txt";
    private static final Pattern IMAGE_PATTERN = Pattern.compile("(https?://.*?\\.(?:png|jpe?g|gif))");
    private static final String OUT_FILE_TXT = "src/outFile.txt";

    public static void main(String[] args) {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(IN_FILE_TXT))) {
            String imageLine = fileReader.readLine();
            String mp3Line = fileReader.readLine();

            Thread imageThread = new Thread(() -> {
                try {
                    String[] imageInfo = imageLine.split(" ");
                    String url = imageInfo[0];
                    String imageSavePath = imageInfo[1];
                    System.out.println(url + "ссылка дпг");
                    String imageUrl = findImageInPage(url);
                    if (imageUrl != null) {
                        downloadImage(imageUrl, imageSavePath + "/image.jpg");
                        System.out.println("Картинка успешно скачана!");
                    } else {
                        System.out.println("Картинка не найдена на странице.");
                    }
                    String filePath = "image.jpg";
                    File file = new File(filePath);
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Изображение успешно скачано!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Thread musicThread = new Thread(() -> {
                String mp3Url = null;
                String mp3SavePath = null;
                int count = 0;
                try (BufferedWriter outFile = new BufferedWriter(new FileWriter(OUT_FILE_TXT))) {
                    String Url;
                    String[] mp3Info = mp3Line.split(" ");
                    mp3Url = mp3Info[0];
                    mp3SavePath = mp3Info[1];
                    System.out.println(mp3SavePath + " ссылка mp3");
                    String result;
                    URL url = new URL(mp3Url);
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                        result = bufferedReader.lines().collect(Collectors.joining("\n"));
                    }
                    Pattern email_pattern = Pattern.compile("(https:)\\/.*\\.mp3");
                    Matcher matcher = email_pattern.matcher(result);
                    int i = 0;
                    while (matcher.find() && i < 1) {
                        outFile.write(matcher.group() + "\r\n");
                        i++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try (BufferedReader musicFile = new BufferedReader(new FileReader(OUT_FILE_TXT))) {
                    String music;

                    try {
                        while ((music = musicFile.readLine()) != null) {
                            downloadUsingNIO(music,  mp3SavePath + "song"+count+".mp3");
                            count++;

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                String musicFilePath = mp3SavePath + "/song"+(count-1)+".mp3";
                try {
                    FileInputStream fileInputStream = new FileInputStream(musicFilePath);
                    Player player = new Player(fileInputStream);
                    player.play();
                    System.out.println("Музыка успешно воспроизводится!");
                } catch (FileNotFoundException | JavaLayerException e) {
                    e.printStackTrace();
                }
            });

            imageThread.start();
            musicThread.start();

            try {
                imageThread.join();
                musicThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void downloadImage(String imageUrl, String filePath) throws IOException {
        URL url = new URL(imageUrl);
        ReadableByteChannel byteChannel = Channels.newChannel(url.openStream());

        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            fileOutputStream.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);
        }
    }
    private static String findImageInPage(String url) throws IOException {
        StringBuilder pageContent = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                pageContent.append(line);
            }
        }
        Matcher matcher = IMAGE_PATTERN.matcher(pageContent.toString());
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }


    private static void downloadUsingNIO(String strUrl, String file) throws IOException {
        URL url = new URL(strUrl);
        ReadableByteChannel byteChannel = Channels.newChannel(url.openStream());
        FileOutputStream stream = new FileOutputStream(file);
        stream.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);
        stream.close();
        byteChannel.close();
    }
}