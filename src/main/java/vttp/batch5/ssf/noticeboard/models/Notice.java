package vttp.batch5.ssf.noticeboard.models;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class Notice {
    @NotBlank(message = "Title cannot be left blank")
    @Size(min = 3, max = 128, message = "Title must be between 3 - 128 characters.")
    private String title;

    @Email(message = "Must be a well-formed email address")
    @NotBlank(message = "Poster cannot be left blank.")
    private String poster;

    @NotNull(message="Date cannot be left blank.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Future(message = "Date posted must be in the future.")
    private Date postDate;

    @NotEmpty(message = "At least one category must be selected.")
    private List<String> categories;

    @NotBlank(message = "Post contents cannot be left blank.")
    private String text;

    public Notice() {
    }

    public Notice(String title, String poster, Date post_date, List<String> categories, String text) {
        this.title = title;
        this.poster = poster;
        this.postDate = post_date;
        this.categories = categories;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date post_date) {
        this.postDate = post_date;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    
}
