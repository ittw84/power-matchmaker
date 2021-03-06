/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker.swingui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerVersion;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.util.BrowserUtil;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MatchMakerSplashScreen {

    private static final Logger logger = Logger.getLogger(MatchMakerSplashScreen.class);

	private JPanel splashScreen;
	private MatchMakerSession session;
	public MatchMakerSplashScreen(MatchMakerSession session) {
		this.session = session;
		buildUI();
	}

    private static class LogoLayout implements LayoutManager {

        int textStartY = 130;
        
        public void layoutContainer(Container parent) {
            JLabel text = (JLabel) parent.getComponent(0);
            JLabel bgLabel = (JLabel) parent.getComponent(1);
            
            bgLabel.setBounds(0, 0, parent.getWidth(), parent.getHeight());
            text.setBounds(0, textStartY, parent.getWidth(), text.getPreferredSize().height);
        }

        public Dimension minimumLayoutSize(Container parent) {
            JLabel bgLabel = (JLabel) parent.getComponent(1);
            return bgLabel.getPreferredSize();
        }

        public Dimension preferredLayoutSize(Container parent) {
            return minimumLayoutSize(parent);
        }

        public void removeLayoutComponent(Component comp) {
            // nop
        }
        
        public void addLayoutComponent(String name, Component comp) {
            // nop
        }
    }

	private void buildUI() {

        JPanel spgLogo = new JPanel(new LogoLayout());
        spgLogo.add(new JLabel("<html><div align='center'>SQL Power Group Inc.<br>http://www.sqlpower.ca/</div></html>", JLabel.CENTER));
        spgLogo.add(new JLabel(new ImageIcon(getClass().getResource("/icons/sqlpower_alpha_gradient.png"))));
        spgLogo.addMouseListener(new MouseAdapter() {
    		@Override
    		public void mouseReleased(MouseEvent e) {
    			try {
    				BrowserUtil.launch(SPSUtils.SQLP_URL);
    			} catch (IOException e1) {
    				throw new RuntimeException("Could not launch web browser with URL " + SPSUtils.SQLP_URL, e1);
    			}
    		}
		});
        
		JLabel mmLogo = new JLabel(SPSUtils.createIcon("dqguru_huge", "DQguru Huge Icon"), JLabel.CENTER);
		JLabel title  = new JLabel("<html>" + "SQL Power DQguru " + MatchMakerVersion.APP_VERSION + "</html>", JLabel.CENTER);
		Font f = title.getFont();
		Font newf = new Font(f.getName(), f.getStyle(), (int) (f.getSize() * 1.5));
		title.setFont(newf);
        

        FormLayout layout = new FormLayout("4dlu, pref:grow, 4dlu ");
		int rowCount =0;
		PanelBuilder pb = new PanelBuilder(layout);
		CellConstraints c = new CellConstraints();
        
		pb.appendRow(new RowSpec("10px:grow"));
		rowCount++;
		
        pb.appendRow(new RowSpec("pref"));
		rowCount++;
        
        pb.add(mmLogo, c.xy(2, rowCount));
        pb.appendRow(new RowSpec("15px"));
        rowCount++;

        pb.appendRow(new RowSpec("pref"));
		rowCount++;
		
        pb.add(title, c.xy(2, rowCount));
		pb.appendRow(new RowSpec("40px"));
		rowCount++;
		
        pb.appendRow(new RowSpec("fill:pref"));
		rowCount++;
		
		pb.appendRow(new RowSpec("40px"));
        rowCount++;
        
        pb.appendRow(new RowSpec("fill:pref"));
        rowCount++;
        
        pb.add(spgLogo, c.xy(2, rowCount));
        rowCount++;
        
        pb.appendRow(new RowSpec("10px:grow"));
        rowCount++;

		splashScreen = pb.getPanel();
	}

	public JPanel getSplashScreen() {
		return splashScreen;
	}
}
