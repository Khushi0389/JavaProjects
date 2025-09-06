import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class UnitConverter {
    private static class Category {
        final String name;
        final String[] units;
        final Converter converter;
        Category(String name, String[] units, Converter c) { this.name=name; this.units=units; this.converter=c; }
    }
    interface Converter { double convert(double val, String from, String to); }

    private final JFrame f = new JFrame("Unit Converter");
    private final JComboBox<String> catBox;
    private final JComboBox<String> fromBox = new JComboBox<>();
    private final JComboBox<String> toBox = new JComboBox<>();
    private final JTextField input = new JTextField("1.0");
    private final JTextField output = new JTextField();

    private final Category[] categories = {
            new Category("Length", new String[]{"m","km","cm","mm","in","ft","yd","mi"},
                    (v,from,to) -> {
                        Map<String,Double> toMeter = new LinkedHashMap<>();
                        toMeter.put("m",1.0); toMeter.put("km",1000.0); toMeter.put("cm",0.01); toMeter.put("mm",0.001);
                        toMeter.put("in",0.0254); toMeter.put("ft",0.3048); toMeter.put("yd",0.9144); toMeter.put("mi",1609.344);
                        return v * toMeter.get(from) / toMeter.get(to);
                    }),
            new Category("Weight", new String[]{"g","kg","lb","oz"},
                    (v,from,to) -> {
                        Map<String,Double> toGram = new LinkedHashMap<>();
                        toGram.put("g",1.0); toGram.put("kg",1000.0); toGram.put("lb",453.59237); toGram.put("oz",28.349523125);
                        return v * toGram.get(from) / toGram.get(to);
                    }),
            new Category("Temperature", new String[]{"C","F","K"},
                    (v,from,to) -> {
                        double c;
                        if (from.equals("C")) c = v;
                        else if (from.equals("F")) c = (v - 32) * 5/9.0;
                        else c = v - 273.15;
                        double out;
                        if (to.equals("C")) out = c;
                        else if (to.equals("F")) out = c * 9/5.0 + 32;
                        else out = c + 273.15;
                        return out;
                    })
    };

    public UnitConverter() {
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(420, 180);
        f.setLocationRelativeTo(null);
        f.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;

        catBox = new JComboBox<>(new String[]{"Length","Weight","Temperature"});
        output.setEditable(false);

        c.gridx=0;c.gridy=0; f.add(new JLabel("Category:"), c);
        c.gridx=1;c.gridy=0; f.add(catBox, c);
        c.gridx=0;c.gridy=1; f.add(new JLabel("From:"), c);
        c.gridx=1;c.gridy=1; f.add(fromBox, c);
        c.gridx=2;c.gridy=1; f.add(new JLabel("To:"), c);
        c.gridx=3;c.gridy=1; f.add(toBox, c);
        c.gridx=0;c.gridy=2; f.add(new JLabel("Value:"), c);
        c.gridx=1;c.gridy=2; f.add(input, c);
        c.gridx=2;c.gridy=2; f.add(new JLabel("Result:"), c);
        c.gridx=3;c.gridy=2; f.add(output, c);

        catBox.addActionListener(e -> loadUnits());
        ActionListener recalc = e -> convert();
        fromBox.addActionListener(recalc);
        toBox.addActionListener(recalc);
        input.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e){ convert(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ convert(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ convert(); }
        });

        loadUnits();
        convert();
        f.setVisible(true);
    }

    private void loadUnits() {
        int idx = catBox.getSelectedIndex();
        fromBox.removeAllItems(); toBox.removeAllItems();
        for (String u : categories[idx].units) { fromBox.addItem(u); toBox.addItem(u); }
        fromBox.setSelectedIndex(0);
        toBox.setSelectedIndex(1 < categories[idx].units.length ? 1 : 0);
    }

    private void convert() {
        try {
            double v = Double.parseDouble(input.getText().trim());
            Category cat = categories[catBox.getSelectedIndex()];
            String from = (String)fromBox.getSelectedItem();
            String to   = (String)toBox.getSelectedItem();
            if (from == null || to == null) return;
            double out = cat.converter.convert(v, from, to);
            output.setText(String.valueOf(out));
        } catch (Exception ex) {
            output.setText("Invalid");
        }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(UnitConverter::new); }
}
