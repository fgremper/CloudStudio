package ch.ethz.fgremper.rtca;

/**
 * Repository info.
 * @author Fabian Gremper
 */
public class RepositoryInfo {

	public String alias;
	public String localPath;

	/**
	 * Initialize RepositoryInfo
	 * @param alias alias of the repository on the RTCA server
	 * @param localPath path to the local git repository
	 */
	public RepositoryInfo(String alias, String localPath) {
		this.alias = alias;
		this.localPath = localPath;
	}

}
