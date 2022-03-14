package test;

import javax.swing.*;
import java.awt.event.ActionListener;

import com.google.zxing.BarcodeFormat;

import com.google.zxing.EncodeHintType;

import com.google.zxing.MultiFormatWriter;

import com.google.zxing.WriterException;

import com.google.zxing.client.j2se.MatrixToImageWriter;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;

import java.awt.*;

import java.awt.event.ActionEvent;

import java.awt.image.BufferedImage;

import java.io.File;

import java.io.FileInputStream;

import java.io.IOException;

import java.net.MalformedURLException;

import java.net.URISyntaxException;

import java.net.URL;

import java.nio.charset.Charset;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;

import java.text.ParseException;

import java.text.SimpleDateFormat;

import java.util.Date;

import java.util.EnumMap;

import java.util.Map;

public class JFrameGui extends JFrame implements ActionListener {
    static String pngPath;
    String[] array = null;
    private JButton open;
    private JButton save;
    private JLabel label;
    private JLabel location;
    private Font font;
    private boolean isSave;

    public JFrameGui() {
        this.getContentPane().add(initPanel(initLabel(), initJButton()));
        setTitle("QrCode");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 400);
        this.setResizable(false);
        setVisible(true);
    }

    private JPanel initPanel(JLabel label, JButton... buttons) {
        JPanel addPanel = new JPanel();
        addPanel.add(label);
        addPanel.add(buttons[0]);
        addPanel.add(buttons[1]);
        addPanel.add(location);
        addPanel.setPreferredSize(new Dimension(420, 400));
        addPanel.setLayout(new FlowLayout(FlowLayout.TRAILING, 23, 100)); //new GridLayout(10, 2, 10, 10)
        addPanel.setBackground(new Color(100, 149, 237));
        setVisible(true);
        return addPanel;
    }

    private JButton[] initJButton() {
        open = new JButton("Select File");
        open.setLocation(12, 371);
        open.setPreferredSize(new Dimension(116, 40));
        open.setBackground(new Color(100, 200, 100));
        open.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        save = new JButton("Save File");
        save.setLocation(12, 371);
        save.setPreferredSize(new Dimension(116, 40));
        save.setBackground(new Color(100, 200, 100));
        save.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        open.addActionListener(this);
        save.addActionListener(this);
        JButton[] buttons = new JButton[2];
        buttons[0] = open;
        buttons[1] = save;
        return buttons;
    }

    private JLabel initLabel() {
        label = new JLabel();
        location = new JLabel();
        label.setText("Please select a CSV file!");
        label.setFont(initFont());
        location.setFont(initFont());
        return label;
    }

    private Font initFont() {
        font = new Font("Arial", Font.PLAIN, 16);
        return font;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == open) {
            JFileChooser chooser = new JFileChooser();
            int response = chooser.showOpenDialog(null);
            if (response == JFileChooser.APPROVE_OPTION) {
                File file = new File(chooser.getSelectedFile().getPath());
                try {
                    if(file == null || file.getAbsoluteFile().equals("")){
                        label.setForeground(Color.RED);
                        label.setText("File not exist or path is wrong");
                        return;
                    }
                    readCSVTime(file);
                    isSave = false;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (e.getSource() == save) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setCurrentDirectory(new File("C:"));
            chooser.setDialogTitle("Save File");
            int response = chooser.showSaveDialog(this);
            if (response == JFileChooser.APPROVE_OPTION) {
                File file = new File(chooser.getSelectedFile().getPath());
                try {
                    if(array == null){
                        label.setForeground(Color.RED);
                        label.setText("File is empty!");
                        return;
                    }
                    formatText(file.getAbsolutePath(), array);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (WriterException writerException) {
                    writerException.printStackTrace();
                } catch (ParseException parseException) {
                    parseException.printStackTrace();
                }
            }
        }
    }


    public void generateQRcode(String data, String path, String charset, Map map, int h, int w, String subText) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, w, h);
        BufferedImage qrCode = MatrixToImageWriter.toBufferedImage(matrix);

        qrCode = drawTextOnImage(subText, qrCode, 30);
        ImageIO.write(qrCode, "png", new File(path));
    }

    private static BufferedImage drawTextOnImage(String text, BufferedImage image, int space) {
        BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight() + space, BufferedImage.TRANSLUCENT);
        Graphics2D g2d = (Graphics2D) bi.createGraphics();
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
        g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON));

        g2d.drawImage(image, 0, 0, null);
        //g2d.setColor(Color.WHITE);
        g2d.fillRect(0, image.getHeight(), bi.getWidth(), bi.getHeight());
        //g2d.setColor(Color.BLACK);
        //g2d.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        //center text at bottom of image in the new space
        g2d.drawString(text, (bi.getWidth() / 2) - textWidth / 2, bi.getHeight() - 10);
        g2d.dispose();
        return bi;
    }


    public void readCSVTime(File file) throws IOException {
        Charset charset = StandardCharsets.ISO_8859_1;
        String fileContent;
        try {
            fileContent = Files.readString(new File(file.getPath()).toPath(), charset);
            array = fileContent.split("\r\n");
            //formatText(array);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void formatText(String path, String... array) throws IOException, WriterException, ParseException {
        Date dt = null;
        SimpleDateFormat inputFormatter = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat outpurFormatter = new SimpleDateFormat("yyyyMMdd");
        String text = "";
        String locPath = "";
        for (int i = 1; i < array.length; i++) {
            String[] record = array[i].split(";");
            //dt = inputFormatter.parse(record[2]);
            //record[2] = outpurFormatter.format(dt);
            if (record[9].length() < 5) {
                record[9] = "00000".substring(record[9].length()) + record[9];
            }
            String phoneNumber = "";
            if (record.length == 11) {
                phoneNumber = record[10];
            }
            String temp = "MTE" + "\t\t" + "01" + "\t\t" + record[0];
            text = temp + "\t" + record[1] + "\t" + record[2] + "\t" + getShortGenderName(record[3]) + "\t" + "\t" + "\t" + "\t" + record[4] + "\t" + record[5] + "\t" + record[6] + "\t" + record[7] + "\t" + getShortCountryName(record[8]) + "\t" + "\t" + "\t" + phoneNumber + "\tp" + record[9] + "\t\r\n";
            String str = text;
            String dir = "FolderForQrCode";
            File filePath = new File(path + "\\" + dir);
            if (!filePath.exists()) {
                filePath.mkdir();
            }
            locPath = filePath.getAbsolutePath();
            System.out.println(filePath.getAbsolutePath());
            label.setForeground(Color.orange);
            label.setText("File loading...");
            pngPath = filePath.getAbsolutePath() + "\\p" + record[9] + ".png";
            String charset = "UTF-8";
            Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new EnumMap<>(EncodeHintType.class);
            hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            generateQRcode(str, pngPath, charset, hashMap, 400, 400, "p" + record[9]);
        }
        label.setForeground(Color.GREEN);
        label.setText("QR code has been generated...");
        location.setForeground(Color.BLACK);
        location.setText(locPath);
    }


    public static String getShortGenderName(String gender) {
        String shortGenderName = "";
        switch (gender) {
            case "männlich":
                shortGenderName = "M";
                break;
            case "weiblich":
            case "Weiblich":
                shortGenderName = "W";
                break;
            case "Divers":
                shortGenderName = "D";
                break;
        }
        return shortGenderName;
    }


    public static String getShortCountryName(String country) {
        String shortCountryNamne = "";
        switch (country) {
            case "Deutschland":
                shortCountryNamne = "D";
                break;
            case "Frankreich":
                shortCountryNamne = "F";
                break;
            case "Österreich":
                shortCountryNamne = "Ö";
                break;
        }
        return shortCountryNamne;
    }


    /**
     * Gets the base location of the given class.
     *
     * <p>
     * <p>
     * If the class is directly on the file system (e.g.,
     * <p>
     * "/path/to/my/package/MyClass.class") then it will return the base directory
     * <p>
     * (e.g., "file:/path/to").
     *
     * </p>
     *
     * <p>
     * <p>
     * If the class is within a JAR file (e.g.,
     * <p>
     * "/path/to/my-jar.jar!/my/package/MyClass.class") then it will return the
     * <p>
     * path to the JAR (e.g., "file:/path/to/my-jar.jar").
     *
     * </p>
     *
     * @param c The class whose location is desired.
     */

    public static URL getLocation(final Class<?> c) {

        if (c == null) {
            return null; // could not load the class
        }


        // try the easy way first

        try {

            final URL codeSourceLocation =

                    c.getProtectionDomain().getCodeSource().getLocation();

            if (codeSourceLocation != null) {
                return codeSourceLocation;
            }

        } catch (final SecurityException e) {

            // NB: Cannot access protection domain.

        } catch (final NullPointerException e) {

            // NB: Protection domain or code source is null.

        }


        // NB: The easy way failed, so we try the hard way. We ask for the class

        // itself as a resource, then strip the class's path from the URL string,

        // leaving the base path.


        // get the class's raw resource path

        final URL classResource = c.getResource(c.getSimpleName() + ".class");

        if (classResource == null) {
            return null; // cannot find class resource
        }


        final String url = classResource.toString();

        final String suffix = c.getCanonicalName().replace('.', '/') + ".class";

        if (!url.endsWith(suffix)) {
            return null; // weird URL
        }


        // strip the class's path from the URL string

        final String base = url.substring(0, url.length() - suffix.length());


        String path = base;


        // remove the "jar:" prefix and "!/" suffix, if present

        if (path.startsWith("jar:")) {
            path = path.substring(4, path.length() - 2);
        }


        try {

            return new URL(path);

        } catch (final MalformedURLException e) {

            e.printStackTrace();

            return null;

        }

    }


    /**
     * Converts the given {@link URL} to its corresponding {@link File}.
     *
     * <p>
     * <p>
     * This method is similar to calling {@code new File(url.toURI())} except that
     * <p>
     * it also handles "jar:file:" URLs, returning the path to the JAR file.
     *
     * </p>
     *
     * @param url The URL to convert.
     * @return A file path suitable for use with e.g. {@link FileInputStream}
     * @throws IllegalArgumentException if the URL does not correspond to a file.
     */

    public static File urlToFile(final URL url) {

        return url == null ? null : urlToFile(url.toString());

    }


    /**
     * Converts the given URL string to its corresponding {@link File}.
     *
     * @param url The URL to convert.
     * @return A file path suitable for use with e.g. {@link FileInputStream}
     * @throws IllegalArgumentException if the URL does not correspond to a file.
     */

    public static File urlToFile(final String url) {

        String path = url;

        if (path.startsWith("jar:")) {

            // remove "jar:" prefix and "!/" suffix

            final int index = path.indexOf("!/");

            path = path.substring(4, index);

        }

        try {

            if (path.matches("file:[A-Za-z]:.*")) {

                path = "file:/" + path.substring(5);

            }

            return new File(new URL(path).toURI());

        } catch (final MalformedURLException e) {

            // NB: URL is not completely well-formed.

        } catch (final URISyntaxException e) {

            // NB: URL is not completely well-formed.

        }

        if (path.startsWith("file:")) {

            // pass through the URL as-is, minus "file:" prefix

            path = path.substring(5);

            return new File(path);

        }

        throw new IllegalArgumentException("Invalid URL: " + url);

    }
}
