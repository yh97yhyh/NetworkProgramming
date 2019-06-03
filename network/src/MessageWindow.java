
import javax.swing.*;
import javax.swing.text.html.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MessageWindow extends JWindow implements ActionListener,
				Runnable, MouseMotionListener, MouseListener {
	JEditorPane display;
	JButton exit;
	JPanel whole;
	Thread runner;
	Dimension screen;
	int x,y,dx,dy;
	Frame frame;
	boolean startDrag;
	public MessageWindow(Frame f, String msg){
		super(f);
		frame = f;
		whole = new JPanel(new BorderLayout());
		whole.setBorder(new EtchedBorder());
		if(frame != null){
		exit = new JButton(new ImageIcon("x.gif")){
			public Dimension getPerferredSize(){
				return new Dimension(18,16);
			}
		};
		} else{
			exit = new JButton("x");
		}
		exit = new JButton("x");
		exit.addActionListener(this);
		JPanel top = new JPanel(new BorderLayout());
		top.add(exit, BorderLayout.EAST);
		top.add(new JLabel("Message Window", JLabel.CENTER), BorderLayout.CENTER);
		StringBuffer buffer = new StringBuffer();
		String from = msg.substring(0, msg.indexOf(":"));
		buffer.append("<center><b>From : ");
		buffer.append(from);
		buffer.append("<hr></b></center>");
		buffer.append(msg.substring(msg.indexOf(":")+4));
		display = new JEditorPane("text/html", buffer.toString());
		display.setEditable(false);
		display.addHyperlinkListener(new Hyperactive());
		whole.add(top, BorderLayout.NORTH);
		whole.add(new JScrollPane(display), BorderLayout.CENTER);
		getContentPane().add(whole, BorderLayout.CENTER);
		Toolkit tk = Toolkit.getDefaultToolkit();
		screen = tk.getScreenSize();
		setLocation(screen.width, screen.height);
		runner = new Thread(this);
		runner.start();
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	public void run(){
		try{
			for(int i=0; i<50; i++){
				Thread.sleep(10);
				setLocation(screen.width-4*i-2, screen.height-4*i-2);
			}
		} catch(Exception e){
			System.err.println(e);
			e.printStackTrace();
		}
	}
	public void actionPerformed(ActionEvent e){
		Object o = e.getSource();
		if(o==exit){
		setVisible(false);
		dispose();
		}
	}
	public void mouseDragged(MouseEvent e){
		dx = x - e.getX();
		dy = y - e.getY();
		Point p = getLocation();
		setLocation(p.x-dx, p.y-dy);
	}
	public void mouseMoved(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e){}
}
class Hyperactive implements HyperlinkListener{
	public void hyperlinkUpdate(HyperlinkEvent e){
		if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
			JEditorPane pane = (JEditorPane)e.getSource();
			if( e instanceof HTMLFrameHyperlinkEvent){
				HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent)e;
				HTMLDocument doc = (HTMLDocument)pane.getDocument();
				doc.processHTMLFrameHyperlinkEvent(evt);
			} else{
				try{
				pane.setPage(e.getURL());
				}catch(Throwable t){
					t.printStackTrace();
				}
			}
		}
	}
}