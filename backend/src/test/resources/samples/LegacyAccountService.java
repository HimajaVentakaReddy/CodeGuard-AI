package samples;

public class LegacyAccountService {
	private String password = "admin123";
	private String apiKey = "sk_live_example_key";

	public void execute(String command) throws Exception {
		// TODO: validate the command before execution
		try {
			Runtime.getRuntime().exec(command);
		} catch (Exception ignored) {
		}
		System.out.println("Command requested");
	}
}
