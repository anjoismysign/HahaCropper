package me.anjoismysign.cropper;

import me.anjoismysign.anjo.entities.Uber;
import me.anjoismysign.anjo.swing.components.AnjoComboBox;
import me.anjoismysign.anjo.swing.components.AnjoTextField;
import me.anjoismysign.anjo.swing.listeners.TextInputType;
import me.anjoismysign.hahaswing.BubbleFactory;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Cropper {
    public static void main(String[] args) {
        openCropper();
    }

    private static void openCropper() {
        Uber<String> sizeLabel = Uber.drive(" (drag a file first)");
        Uber<List<File>> toCrop = Uber.drive(new ArrayList<>());
        AnjoTextField elements = AnjoTextField.build("Elements " + sizeLabel.thanks())
                .addColorToText(TextInputType.INTEGER, Color.RED, false)
                .addColorToText(TextInputType.INTEGER, Color.BLACK, true);
        BubbleFactory.getInstance().controller(anjoPane -> {
                            int random = new Random().nextInt(5);
                            if (random > 0) {
                                System.exit(0);
                                return;
                            }
                            JOptionPane.showMessageDialog(null, "Thank you for using HahaCropper!\n" +
                                    "You might also be interested in no copyright, non disruptive electronic dance music\n" +
                                    "If so, check 'anjoismysignature' music album", "HahaCropper", JOptionPane.INFORMATION_MESSAGE);
                            System.exit(0);
                        },
                        "HahaCropper",
                        new ImageIcon(Objects.requireNonNull(Cropper.class.getResource("/anjoismysignature.png")))
                                .getImage().getScaledInstance(256, 256, Image.SCALE_SMOOTH),
                        true,
                        file -> {
                            boolean isDirectory = file.isDirectory();
                            boolean isPng = file.getName().endsWith(".png");
                            if (!isPng) {
                                if (isDirectory) {
                                    File[] pictures = file.listFiles((dir, name) -> name.endsWith("png"));
                                    toCrop.talk(pictures == null ? new ArrayList<>() : List.of(pictures));
                                    return;
                                }
                                JOptionPane.showMessageDialog(null, "Only PNG files are supported.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            toCrop.talk(List.of(file));
                        },
                        elements,
                        AnjoComboBox.build("Split", List.of("12x", "16x", "32x")))
                .onBlow(anjoPane -> {
                    List<File> croppable = toCrop.thanks();
                    if (croppable == null || croppable.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No file were selected.", "Error", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                        return;
                    }
                    int size = anjoPane.getInteger(0).toOptional().orElse(0);
                    if (size < 2) {
                        JOptionPane.showMessageDialog(null, "Invalid size.", "Error", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                        return;
                    }
                    String parseSplit = anjoPane.getComboBoxText(1).substring(0, 2);
                    int split;
                    try {
                        split = Integer.parseInt(parseSplit);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "Invalid split.", "Error", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                        return;
                    }
                    System.out.println("Cropping " + croppable.size() + " file(s) with size " + size + " and split " + split + "x");
                    croppable.forEach(file -> {
                        cropImage(file, size, split);
                    });
                    openCropper();
                });
    }

    private static void cropImage(@NotNull File file,
                                  int size,
                                  int split) {
        Image image = new ImageIcon(file.getAbsolutePath()).getImage();
        final String fileName = file.getName();
        System.out.println(file.getPath());
        // Convert Image to BufferedImage
        BufferedImage sourceBufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = sourceBufferedImage.getGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        size = split * size;

        BufferedImage destinationBufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = destinationBufferedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics2D.drawImage(sourceBufferedImage, 0, 0, size, size, null);
        graphics2D.dispose();

        if (size > split) {
            int rows = size / split; // The values for rows and cols would depend on your image's pixel size
            int cols = size / split;
            int count = 1;
            File downloadsFolder = new File(System.getProperty("user.home") + "/Downloads");
            File outputFolder = new File(downloadsFolder, fileName.replace(".png", ""));
            outputFolder.mkdirs();
            for (int x = 0; x < rows; x++) {
                for (int y = 0; y < cols; y++) {
                    // Initialize the image array with image chunks
                    BufferedImage img = new BufferedImage(split, split, destinationBufferedImage.getType());

                    // draws the image chunk
                    Graphics2D gr = img.createGraphics();
                    gr.drawImage(destinationBufferedImage, 0, 0, split, split, split * y, split * x, split * y + split, split * x + split, null);
                    gr.dispose();

                    // Write the BufferedImage to a file in the Downloads folder
                    File outputFile = new File(outputFolder, count + ".png");
                    try {
                        ImageIO.write(img, "png", outputFile);
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    count++;
                }
            }
        } else {
            // Write the BufferedImage to a file in the Downloads folder
            File downloadsFolder = new File(System.getProperty("user.home") + "/Downloads");
            File outputFile = new File(downloadsFolder, fileName);
            try {
                ImageIO.write(destinationBufferedImage, "png", outputFile);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
                return;
            }
        }
    }
}