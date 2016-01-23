package dataTypes;
import java.util.ArrayList;
import java.util.List;

public class Movie {

	private String name;
	private String description;
	private int year;
	private List<Actor> starring = new ArrayList<Actor>();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<Actor> getStarring() {
		return starring;
	}
	public void setStarring(List<Actor> starring) {
		this.starring = starring;
	}
}
