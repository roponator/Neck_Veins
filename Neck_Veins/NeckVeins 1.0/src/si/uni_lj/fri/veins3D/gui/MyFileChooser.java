package si.uni_lj.fri.veins3D.gui;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MyFileChooser extends JFrame
{
	

	public MyFileChooser()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JButton button = new JButton("Show the file chooser");
		final JFileChooser chooser = new JFileChooser();
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				chooser.showOpenDialog(MyFileChooser.this);
			}
		});
		getContentPane().add(button, BorderLayout.CENTER);
		pack();
		setVisible(true);
	}
}