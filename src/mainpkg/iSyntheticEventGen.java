package mainpkg;
import java.util.List;

public interface iSyntheticEventGen {
	public void receive(List<String> event);
	public void dataSetOver();
}
