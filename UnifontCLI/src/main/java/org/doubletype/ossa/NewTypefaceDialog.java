package org.doubletype.ossa;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import javax.swing.*;
import java.awt.ComponentOrientation;
import javax.swing.JButton;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ResourceBundle;

public class NewTypefaceDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private final String k_punctPattern =
		".*[!\\\"#$%&'()*+,/:;<=>?@\\\\^`{|}~].*"; 
	
	private String m_msgNoPunct = "no punct";
	private String m_msgFileNameRequired = "File name is required.";  //  @jve:decl-index=0:
	private String m_msgTypefaceExistsInDir = "A typeface already exists in the folder.";
	private String m_msgFailedToCreateDirs = "Failed to create folder: %s";
	private String m_fileName = null;
	private String m_oldFileName = "";
	private File m_dir;
	
	
	private JPanel jContentPane = null;

	private JPanel pnlMain = null;

	private JLabel m_lblTypefaceFileName = null;

	private JTextField m_txtTypefaceFileName = null;

	private JOptionPane m_optionPane = null;


	private JLabel m_lblTypefaceDirName = null;

	private JTextField m_txtTypefaceDirName = null;

	private JButton btnDirChooser = null;
	
	private JFileChooser m_fileChooser = null;
	
	/**
	 * @param owner
	 */
	public NewTypefaceDialog(Frame owner) {
		super(owner);
		initialize();
		beforeShow();
	}
	
	/**
	 * @param a_owner
	 * @param a_modal
	 */
	public NewTypefaceDialog(Frame a_owner, boolean a_modal) {
		super(a_owner, a_modal);
		initialize();
		beforeShow();
	}
	
	private void beforeShow() {
		m_fileChooser = new JFileChooser();
		m_fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		File f = new File(new File("").getAbsolutePath());
		
		m_txtTypefaceDirName.setText(f.getAbsolutePath());
		localize(Engine.getSingletonInstance().getBundle());
	}
	
	public void localize(ResourceBundle a_bundle) {
		m_msgNoPunct = a_bundle.getString("msgNoPunct");
		m_msgFileNameRequired = String.format(a_bundle.getString("msgIsRequired"),
				a_bundle.getString("fileName"));
		m_msgTypefaceExistsInDir = a_bundle.getString("msgTypefaceExistsInDir");
		m_lblTypefaceFileName.setText(a_bundle.getString("typefaceFileName"));
		m_lblTypefaceDirName.setText(a_bundle.getString("typefaceDir"));
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(457, 153);
		this.setResizable(false);
		this.setModal(true);
		this.setName("New Typeface");
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getPnlMain(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes pnlMain	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPnlMain() {
		if (pnlMain == null) {
			m_lblTypefaceDirName = new JLabel();
			m_lblTypefaceDirName.setBounds(new Rectangle(15, 45, 121, 16));
			m_lblTypefaceDirName.setText("Typeface Folder");
			m_lblTypefaceDirName.setFont(new Font("Dialog", Font.PLAIN, 12));
			m_lblTypefaceFileName = new JLabel();
			m_lblTypefaceFileName.setBounds(new Rectangle(15, 15, 118, 16));
			m_lblTypefaceFileName.setFont(new Font("Dialog", Font.PLAIN, 12));
			m_lblTypefaceFileName.setText("Typeface File Name");
			pnlMain = new JPanel();
			pnlMain.setLayout(null);
			pnlMain.setPreferredSize(new Dimension(1, 1));
			pnlMain.setFont(new Font("SansSerif", Font.PLAIN, 12));
			pnlMain.add(m_lblTypefaceFileName, null);
			pnlMain.add(getM_txtTypefaceFileName(), null);
			pnlMain.add(getM_optionPane(), null);
			pnlMain.add(m_lblTypefaceDirName, null);
			pnlMain.add(getM_txtTypefaceDirName(), null);
			pnlMain.add(getBtnDirChooser(), null);
		}
		return pnlMain;
	}

	/**
	 * This method initializes m_txtTypefaceFileName	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getM_txtTypefaceFileName() {
		if (m_txtTypefaceFileName == null) {
			m_txtTypefaceFileName = new JTextField();
			m_txtTypefaceFileName.setBounds(new Rectangle(135, 15, 286, 20));
			m_txtTypefaceFileName.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyTyped(java.awt.event.KeyEvent e) {
					Timer t = new Timer(1, new ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							txtTypefaceFileNameKeyTyped();
						}
					});
					t.setRepeats(false);
					t.start();
				}
				
				
			});
		}
		return m_txtTypefaceFileName;
	}
	
	private void txtTypefaceFileNameKeyTyped() {
		String s = m_txtTypefaceFileName.getText();
		
		if (s.matches(k_punctPattern)) {
			JOptionPane.showMessageDialog(null, m_msgNoPunct);
			m_txtTypefaceFileName.setText(m_oldFileName);
			return;
		} // if
		
		m_oldFileName = s;
		File f = new File(new File(new File("").getAbsolutePath()), s);
		m_txtTypefaceDirName.setText(f.getAbsolutePath());		
	}

	/**
	 * This method initializes m_optionPane	
	 * 	
	 * @return javax.swing.JOptionPane	
	 */
	private JOptionPane getM_optionPane() {
		if (m_optionPane == null) {
			m_optionPane = new JOptionPane();
			m_optionPane.setBounds(new Rectangle(0, 75, 451, 46));
			m_optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
			m_optionPane.setComponentOrientation(ComponentOrientation.UNKNOWN);
			m_optionPane.setMessageType(JOptionPane.PLAIN_MESSAGE);
			m_optionPane.setMessage("");
			m_optionPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
					if ((e.getPropertyName().equals("value"))) {
						Object value = e.getNewValue();
						if (value.equals(new Integer(JOptionPane.CANCEL_OPTION))) {
							hideDialog();
							return;
						} // if
						
						if (value.equals(new Integer(JOptionPane.OK_OPTION))) {
							validateAndHide();
							return;
						} // if
					} // if
				}
			});
		}
		return m_optionPane;
	}
	
	private void hideDialog() {
		setVisible(false);
	}
	
	public String getFileName() {
		return m_fileName;
	}
	
	public File getDir() {
		return m_dir;
	}
	
	private void validateAndHide() {
		String fileName = m_txtTypefaceFileName.getText();
		File dir = new File(m_txtTypefaceDirName.getText());
		
		String errorMessage = "";
		if ((fileName == null) || (fileName.equals(""))) {
			errorMessage += m_msgFileNameRequired;
		} // if
		
		if (dir.exists()) {
			String fileNames[] = dir.list();
			for (int i = 0; i < fileNames.length; i++) {
				String s = fileNames[i];
				if (s.toLowerCase().endsWith(".glyph")
						|| s.toLowerCase().endsWith(".dtyp")) {
					errorMessage += m_msgTypefaceExistsInDir;
					break;
				} // if
			} // for i
		} // if
		
		if (!errorMessage.equals("")) {
			JOptionPane.showMessageDialog(this, errorMessage,
				"Error", JOptionPane.ERROR_MESSAGE);
			m_optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
			return;
		} // if
		
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				errorMessage = String.format(m_msgFailedToCreateDirs, dir.toString());
				JOptionPane.showMessageDialog(this, errorMessage,
						"Error", JOptionPane.ERROR_MESSAGE);
				m_optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
				return;
			} // if
		} // if
		
		m_fileName = fileName;
		m_dir = dir;
		hideDialog();
	}

	/**
	 * This method initializes m_txtTypefaceDirName	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getM_txtTypefaceDirName() {
		if (m_txtTypefaceDirName == null) {
			m_txtTypefaceDirName = new JTextField();
			m_txtTypefaceDirName.setBounds(new Rectangle(135, 45, 271, 20));
		}
		return m_txtTypefaceDirName;
	}

	/**
	 * This method initializes btnDirChooser	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBtnDirChooser() {
		if (btnDirChooser == null) {
			btnDirChooser = new JButton();
			btnDirChooser.setText("...");
			btnDirChooser.setSize(new Dimension(31, 20));
			btnDirChooser.setLocation(new Point(405, 45));
			btnDirChooser.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					onBtnDirChooserClicked();
				}
			});
		}
		return btnDirChooser;
	}
	
	private void onBtnDirChooserClicked() {
		m_fileChooser.setSelectedFile(
			new File(m_txtTypefaceDirName.getText()));
		if (JFileChooser.APPROVE_OPTION != m_fileChooser.showSaveDialog(this)) {
			return;
		} // if
		
		m_txtTypefaceDirName.setText(
				m_fileChooser.getSelectedFile().getAbsolutePath());
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
