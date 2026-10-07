package samples;

public class CleanAccountService {

	public boolean isAccountActive(boolean active, boolean suspended) {
		return active && !suspended;
	}
}
