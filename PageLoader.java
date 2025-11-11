package com.acme;

import com.github.prominence.openweathermap.api.OpenWeatherMapClient;
import com.github.prominence.openweathermap.api.enums.Language;
import com.github.prominence.openweathermap.api.enums.UnitSystem;
import com.github.prominence.openweathermap.api.model.forecast.Forecast;
import com.github.prominence.openweathermap.api.model.forecast.WeatherForecast;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Path("") //Non necessario ma serve al browser
public class PageLoader {
    private static final String pathUsersTxt = "C:\\Users\\VladyslavBukator\\OneDrive - ITS Incom\\Documenti\\Lezioni ITSINCOM\\Java\\Website-Login-Weather\\src\\main\\resources\\user_log";
    private static final String pathFavCities = "C:\\Users\\VladyslavBukator\\OneDrive - ITS Incom\\Documenti\\Lezioni ITSINCOM\\Java\\Website-Login-Weather\\src\\main\\resources\\user_fav_city";
    private final Template login, signup, welcome, forecast, favoriteCity;
    private int loggedUserID = 0;

    public PageLoader(Template login, Template signup, Template welcome, Template forecast, Template favoriteCity) {
        this.login = login;
        this.signup = signup;
        this.welcome = welcome;
        this.forecast = forecast;
        this.favoriteCity = favoriteCity;
    }

    // Login Page --------------------------------------------------------------
    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public Response loadLoginPage() {
        return Response.ok(login.render()).build();
    }

    @POST
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance loginPage(@FormParam("email") String email, @FormParam("password") String password) {
        List<Person> users = readUsersFile();
        List <String> cities = readUserFavCities();
        List <String> newCities = new ArrayList<>();

        for (String x : cities) {
            String[] breakLineCity = x.split(",");

            if (Integer.parseInt(breakLineCity[0]) == loggedUserID) {
                newCities.add(breakLineCity[1]);
            }
        }

        for (Person x : users) {
            if (x.getEmail().equals(email) && x.getPassword().equals(password)) {
                loggedUserID = Integer.parseInt(x.getUserID());
                return welcome.data("username", x.getUsername()).data("cities", newCities);
            }
        }

        return login.data("error", "Invalid email or password. Please try again.");
    }

    // Register Page --------------------------------------------------------------

    @GET
    @Path("/signup")
    @Produces(MediaType.TEXT_HTML)
    public Response signupPage() {
        return Response.ok(signup.render()).build();
    }

    @POST
    @Path("/signup")
    @Produces(MediaType.TEXT_HTML)
    public Response signupPage(@FormParam("username") String username, @FormParam("email") String email, @FormParam("password") String password) {
        try {
            List<Person> users = readUsersFile();

            for (Person x : users) {
                if (x.getUsername().equals(username) && x.getEmail().equals(email) && x.getPassword().equals(password)) {
                    return Response.ok(signup.render()).build();
                }
            }

            FileWriter fWriter = new FileWriter(pathUsersTxt, true);
            fWriter.write( users.size() + "," + username + "," + email + "," + password + "\n");
            fWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Response.ok(login.render()).build();
    }

    // Welcome Page --------------------------------------------------------------

    @GET
    @Path("/welcome")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance welcomePage() { return welcome.data("guest", "Guest"); }

    @POST
    @Path("/welcome")
    @Produces(MediaType.TEXT_HTML)
    public Response welcomePage(@FormParam("city") String city) {
        List<Person> users = readUsersFile();

        for (Person user : users) {
            if (Integer.parseInt(user.getUserID()) == loggedUserID) {
                try {
                    FileWriter fWriter = new FileWriter(pathFavCities, true);
                    fWriter.write(user.getUserID() + "," + city + "\n");
                    fWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        OpenWeatherMapClient openWeatherClient = new OpenWeatherMapClient("3f63263d298b035708f4305cf4e75f70");

        final Forecast forecastApi = openWeatherClient
                .forecast5Day3HourStep()
                .byCityName(city)
                .language(Language.ITALIAN)
                .unitSystem(UnitSystem.METRIC)
                .count(15)
                .retrieve()
                .asJava();

        List<WeatherForecast> weatherForecasts = forecastApi.getWeatherForecasts();
        LocalDate lastForeCast = LocalDate.now();

        TemplateInstance template = forecast
                .data(
                        "citta", city,
                        "forecasts", weatherForecasts
                );
        return Response
                .ok(template)
                .build();
    }

    // Favorite City Page --------------------------------------------------------------

    @POST
    @Path("/favoriteCity")
    @Produces(MediaType.TEXT_HTML)
    public Response favoriteCity(@FormParam("city") String city) {
        OpenWeatherMapClient openWeatherClient = new OpenWeatherMapClient("3f63263d298b035708f4305cf4e75f70");
        List <String> cities = readUserFavCities();
        List<Person> users = readUsersFile();

        if (city == null || city.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("City is required!").build();
        }

        final Forecast forecastApi = openWeatherClient
                .forecast5Day3HourStep()
                .byCityName(city)
                .language(Language.ITALIAN)
                .unitSystem(UnitSystem.METRIC)
                .count(15)
                .retrieve()
                .asJava();

        List<WeatherForecast> weatherForecasts = forecastApi.getWeatherForecasts();
        LocalDate lastForeCast = LocalDate.now();

        TemplateInstance template = favoriteCity
                .data("cities", cities)
                .data("city", city)
                .data("forecasts", weatherForecasts);

        return Response
                .ok(template)
                .build();
    }

    // Functions --------------------------------------------------------------

    public static List<Person> readUsersFile(){
        List <Person> users = new ArrayList<>();

        try {
            File file = new File(pathUsersTxt);
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] breakLine = line.split(",");
                users.add(new Person(breakLine[0], breakLine[1], breakLine[2], breakLine[3]));
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public static List<String> readUserFavCities(){
        List <String> cities = new ArrayList<>();

        try {
            File file = new File(pathFavCities);
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] breakLine = line.split(",");
                cities.add(line);
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cities;
    }
}
