package com.cineverse.cineversebackend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "theaters")
public class Theater {
    @Id
    private String id;
    private String name;
    private String city;
    private String address;
    private List<Screen> screens = new ArrayList<>();

    public Theater() {}

    public Theater(String id, String name, String city, String address, List<Screen> screens) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.screens = screens != null ? screens : new ArrayList<>();
    }

    public static TheaterBuilder builder() {
        return new TheaterBuilder();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public List<Screen> getScreens() { return screens; }
    public void setScreens(List<Screen> screens) { this.screens = screens; }

    public static class Screen {
        private String id;
        private String screenNumber;
        private int rows;
        private int cols;

        public Screen() {}

        public Screen(String id, String screenNumber, int rows, int cols) {
            this.id = id;
            this.screenNumber = screenNumber;
            this.rows = rows;
            this.cols = cols;
        }

        public static ScreenBuilder builder() {
            return new ScreenBuilder();
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getScreenNumber() { return screenNumber; }
        public void setScreenNumber(String screenNumber) { this.screenNumber = screenNumber; }

        public int getRows() { return rows; }
        public void setRows(int rows) { this.rows = rows; }

        public int getCols() { return cols; }
        public void setCols(int cols) { this.cols = cols; }

        public static class ScreenBuilder {
            private String id;
            private String screenNumber;
            private int rows;
            private int cols;

            public ScreenBuilder id(String id) { this.id = id; return this; }
            public ScreenBuilder screenNumber(String screenNumber) { this.screenNumber = screenNumber; return this; }
            public ScreenBuilder rows(int rows) { this.rows = rows; return this; }
            public ScreenBuilder cols(int cols) { this.cols = cols; return this; }

            public Screen build() {
                return new Screen(id, screenNumber, rows, cols);
            }
        }
    }

    public static class TheaterBuilder {
        private String id;
        private String name;
        private String city;
        private String address;
        private List<Screen> screens;

        public TheaterBuilder id(String id) { this.id = id; return this; }
        public TheaterBuilder name(String name) { this.name = name; return this; }
        public TheaterBuilder city(String city) { this.city = city; return this; }
        public TheaterBuilder address(String addr) { this.address = addr; return this; }
        public TheaterBuilder screens(List<Screen> scr) { this.screens = scr; return this; }

        public Theater build() {
            return new Theater(id, name, city, address, screens);
        }
    }
}
