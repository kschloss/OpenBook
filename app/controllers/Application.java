package controllers;

import java.util.*;

import play.*;
import play.mvc.*;
import controllers.Secure;
import models.*;

@With(Secure.class)
public class Application extends Controller {

	@Before
	static void addDefaults() {
	}

	public static User user() {
		assert Secure.Security.connected() != null;
		return User.find("byEmail", Secure.Security.connected()).first();
	}

	public static void about(Long id) {
		User user = id == null ? user() : (User) User.findById(id);
		render(user);
	}

	public static void news(Long id) {
		User user = id == null ? user() : (User) User.findById(id);
		render(user);
	}

	public static void account() {
		User user = user();
		render(user);
	}

	private static boolean given(String val) {
		return val != null && val.length() > 0;
	}
	
	public static void requestFriends(Long id) {
		User user = user();
		User other = User.find("id = ?", id).first();
		Relationship r1 = Relationship.find("SELECT r FROM Relationship r where r.from = ? AND r.to = ?", user, other).first();
		Relationship r2 = Relationship.find("SELECT r FROM Relationship r where r.to = ? AND r.from = ?", user, other).first();
		
		// Update the user making the request 
		if (r2 != null) {
			r1 = new Relationship(user, other, true);
			
			// If the other user has requested, this request should make them friends
			if (r2.requested) {
				r2.accepted = true;
				r2.requested = false;
				r1.accepted = true;
				r2.save();
			} else if (r2.accepted){
				news(id);
				return;
			} else {
				r1.requested = true;
			}
		} else if (r1 != null && r1.requested) {
			news(id);
			return;
		} else {
			r1 = new Relationship(user, other, true);
		}
		user.save();
		other.save();
		r1.save();
		news(id);
	}

	public static void account_save(User update, String old_password) {
		User currentUser = user();

		validation.required(update.first_name).message("First name is required");
		validation.required(update.username).message("Username is required");
		validation.required(update.email).message("Email is required");
		validation.isTrue(currentUser.password.equals(old_password)).message(
				"Password does not match");
		if (validation.hasErrors()) {
			User user = update;
			renderTemplate("Application/account.html", user);
		} else {
			User user = currentUser;
			String name = "";
			if (given(update.first_name)) {
				user.first_name = update.first_name;
				name += user.first_name;
			}
			if (given(update.middle_name)) {
				user.middle_name = update.middle_name;
				if (given(name))
					name += " ";
				name += user.first_name;
			}
			if (given(update.last_name)) {
				user.last_name = update.last_name;
				if (given(name))
					name += " ";
				name += user.first_name;
			}
			user.name = name;
			user.username = update.username;
			user.email = update.email;
			if (given(update.password))
				user.password = update.password;
			user.save();
			account();
		}
	}

	public static void edit_basic() {
		long userID = 1;
		User user = User.findById(userID);
		render(user);
	}

	public static void updateBasic() {
		long userID = 1;
		User user = User.findById(userID);
		render(user);
	}

	public static void search(String query) {
		// not implemented yet
	}

	public static void deleteComment(Long id, Long page) {
		Comment c = Comment.findById(id);
		c.delete();
		news(page);
	}

	public static void postComment(Long postId, String author, String content) {
		Post post = Post.findById(postId);
		post.addComment(author, content);
	}
}