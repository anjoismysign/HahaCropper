package me.anjoismysign.cropper;

import me.anjoismysign.anjo.entities.Uber;
import me.anjoismysign.anjo.swing.components.AnjoComboBox;
import me.anjoismysign.anjo.swing.components.AnjoTextField;
import me.anjoismysign.anjo.swing.listeners.TextInputType;
import me.anjoismysign.hahaswing.BubbleFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Cropper {
    public static void main(String[] args) {
        openCropper();
    }

    private static void openCropper() {
        Uber<String> downscale = Uber.drive(" (drag a file first)");
        Uber<Image> toCrop = Uber.fly();
        Uber<Integer> toCropSize = Uber.drive(0);
        Uber<String> toCropFileName = Uber.fly();
        AnjoTextField downscaleField = AnjoTextField.build("Downscale " + downscale.thanks())
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
                            boolean isPng = file.getName().endsWith(".png");
                            if (!isPng) {
                                JOptionPane.showMessageDialog(null, "Only PNG files are supported.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            Image image = new ImageIcon(file.getAbsolutePath()).getImage();
                            int width = image.getWidth(null);
                            int height = image.getHeight(null);
                            if (width != height) {
                                JOptionPane.showMessageDialog(null, "Only square images are supported.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            toCropSize.talk(width);
                            toCropFileName.talk(file.getName().replace(".png", "") + "-cropped.png");
                            downscale.talk("Downscale (size: " + width + ")");
                            downscaleField.getLabel().setText(downscale.thanks());
                            toCrop.talk(image);
                        },
                        downscaleField,
                        AnjoComboBox.build("Split", List.of("12x", "16x", "32x")))
                .onBlow(anjoPane -> {
                    Image image = toCrop.thanks();
                    if (image == null) {
                        JOptionPane.showMessageDialog(null, "No image was selected.", "Error", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                        return;
                    }
                    int size = anjoPane.getInteger(0).toOptional().orElse(0);
                    if (size == 0) {
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

                    // Convert Image to BufferedImage
                    BufferedImage sourceBufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                    Graphics graphics = sourceBufferedImage.getGraphics();
                    graphics.drawImage(image, 0, 0, null);
                    graphics.dispose();

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
                        File outputFolder = new File(downloadsFolder, toCropFileName.thanks().replace(".png", ""));
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
                        openCropper();
                    } else {
                        // Write the BufferedImage to a file in the Downloads folder
                        File downloadsFolder = new File(System.getProperty("user.home") + "/Downloads");
                        File outputFile = new File(downloadsFolder, toCropFileName.thanks());
                        try {
                            ImageIO.write(destinationBufferedImage, "png", outputFile);
                            openCropper();
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            System.exit(0);
                            return;
                        }
                    }
                });
    }
}