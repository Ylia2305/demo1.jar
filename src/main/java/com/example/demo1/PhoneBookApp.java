package com.example.demo1;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PhoneBookApp extends Application {

    private PhoneBook phoneBook;

    private ObservableList<Contact> contactList;

    private ListView<Contact> listView;



    private void showAllContacts() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Все контакты");
        alert.setHeaderText(null);

        StringBuilder content = new StringBuilder();

        for (Contact contact : phoneBook.getAllContacts()) {
            content.append("Имя: ").append(contact.getName()).append("\n");
            content.append("Фамилия: ").append(contact.getLastName()).append("\n");
            content.append("Отчество: ").append(contact.getMiddleName()).append("\n");
            content.append("Телефоны: ").append("\n");

            for (String phoneNumber : contact.getPhoneNumbers()) {
                content.append("- ").append(phoneNumber).append("\n");
            }

            content.append("\n");
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    @Override

    public void start(Stage primaryStage) {
        // Загрузка базы данных из файла
        phoneBook = loadPhoneBook();

        // Создание списка контактов для отображения в ListView
        contactList = FXCollections.observableArrayList(phoneBook.getAllContacts());
        Comparator<Contact> contactComparator = new Comparator<Contact>() {
            @Override

            public int compare(Contact contact1, Contact contact2) {
                // Здесь Вы можете определить логику сравнения двух контактов
                // Например, сравнивать по имени или фамилии
                String name1 = contact1.getName();
                String name2 = contact2.getName();
                return name1.compareToIgnoreCase(name2);

            }
        };

        contactList.sort(contactComparator);

        // Создание графического интерфейса
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setOnScroll(event -> {
            double deltaY = event.getDeltaY(); // получаем изменение скроллинга по оси Y
            if (deltaY < 0) {
                gridPane.setScaleX(gridPane.getScaleX() * 0.9); // уменьшаем масштаб при прокручивании вниз
                gridPane.setScaleY(gridPane.getScaleY() * 0.9);
            } else {
                gridPane.setScaleX(gridPane.getScaleX() * 1.1); // увеличиваем масштаб при прокручивании вверх
                gridPane.setScaleY(gridPane.getScaleY() * 1.1);
            }
        });
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        listView = new ListView<>(contactList);
        listView.setCellFactory(param -> new ListCell<Contact>() {
            @Override
            protected void updateItem(Contact contact, boolean empty) {
                super.updateItem(contact, empty);

                if (empty || contact == null) {
                    setText(null);
                } else {
                    StringBuilder text = new StringBuilder();
                    text.append("Имя: ").append(contact.getName()).append("\n");
                    text.append("Фамилия: ").append(contact.getLastName()).append("\n");
                    text.append("Отчество: ").append(contact.getMiddleName()).append("\n");
                    text.append("Телефоны: ").append("\n");

                    for (String phoneNumber : contact.getPhoneNumbers()) {
                        text.append("- ").append(phoneNumber).append("\n");
                    }

                    setText(text.toString());
                }
            }
        });
        listView.setPrefWidth(300);
        listView.setPrefHeight(200);


        Button viewButton = new Button("Просмотреть");
        viewButton.setOnAction(e -> showAllContacts());

        gridPane.add(listView, 0, 0, 3, 1);
        gridPane.add(viewButton, 0, 2);

        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> {
            showAddContactDialog();
            savePhoneBook(phoneBook);
        });

        Button deleteButton = new Button("Удалить");
        deleteButton.setOnAction(e -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();

            if (selectedIndex >= 0) {
                Contact selectedContact = contactList.get(selectedIndex);
                phoneBook.removeContact(selectedContact);
                contactList.remove(selectedIndex);
                savePhoneBook(phoneBook);
            }
        });

        Button searchButton = new Button("Поиск");
        searchButton.setOnAction(e -> showSearchResultDialog());

        gridPane.add(searchButton, 3, 2);
        Button sortByNameButton = new Button("Сортировать по имени");
        sortByNameButton.setOnAction(e -> {
            contactList.sort(contactComparator);
            savePhoneBook(phoneBook);
        });

        Button sortBySurnameButton = new Button("Сортировать по фамилии");
        sortBySurnameButton.setOnAction(e -> {
            contactList.sort(contactComparator);
            savePhoneBook(phoneBook);
        });

        Button sortByPatronymicButton = new Button("Сортировать по отчеству");
        sortByPatronymicButton.setOnAction(e -> {
            Comparator<Contact> contactPatronymicComparator = Comparator.comparing(Contact::getMiddleName);
            contactList.sort(contactComparator);
            savePhoneBook(phoneBook);
            ; // сохранение данных при сортировке по отчеству
        });

        gridPane.add(sortByNameButton, 0, 3);
        gridPane.add(sortBySurnameButton, 1, 3);
        gridPane.add(sortByPatronymicButton, 2, 3);
        sortByNameButton.setOnAction(e -> sortContactsByName());
        sortBySurnameButton.setOnAction(e -> sortContactsBySurname());
        sortByPatronymicButton.setOnAction(e -> sortContactsByPatronymic());

        Button exitButton = new Button("Выход");
        exitButton.setOnAction(e -> {
            savePhoneBook(phoneBook);
            primaryStage.close();
        });

        gridPane.add(addButton, 0, 1);
        gridPane.add(exitButton, 1, 1);
        gridPane.add(deleteButton, 2, 2);

        Scene scene = new Scene(gridPane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Телефонный справочник");
        primaryStage.show();
    }

    private void sortContactsByName() {
        Comparator<Contact> nameComparator = Comparator.comparing(Contact::getName)
                .thenComparing(Contact::getLastName)
                .thenComparing(Contact::getMiddleName);
        List<Contact> sortedContacts = new ArrayList<>(contactList);
        sortedContacts.sort(nameComparator);
        contactList.setAll(sortedContacts);
    }

    private void sortContactsBySurname() {
        Comparator<Contact> surnameComparator = Comparator.comparing(Contact::getLastName)
                .thenComparing(Contact::getName)
                .thenComparing(Contact::getMiddleName);
        List<Contact> sortedContacts = new ArrayList<>(contactList);
        sortedContacts.sort(surnameComparator);
        contactList.setAll(sortedContacts);
    }

    private void sortContactsByPatronymic() {
        Comparator<Contact> patronymicComparator = Comparator.comparing(Contact::getMiddleName)
                .thenComparing(Contact::getName)
                .thenComparing(Contact::getLastName);
        List<Contact> sortedContacts = new ArrayList<>(contactList);
        sortedContacts.sort(patronymicComparator);
        contactList.setAll(sortedContacts);
    }

    private void showSearchResultDialog() {
        String keyword = showInputDialog();
        List<Contact> searchResult = searchContacts(keyword);
        if (!searchResult.isEmpty()) {
            showSearchResults(searchResult);
        } else {
            showNoResultsMessage();
        }
    }

    private String showInputDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Поиск абонента");
        dialog.setHeaderText("Введите ключевое слово (имя или фамилия или отсество)");
        dialog.setContentText("Ключевое слово:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse("");
    }

    private List<Contact> searchContacts(String keyword) {
        return phoneBook.searchByKeyword(keyword);
    }

    private void showSearchResults(List<Contact> searchResult) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Результаты поиска");
        alert.setHeaderText(null);

        StringBuilder content = new StringBuilder();
        for (Contact contact : searchResult) {
            content.append("Имя: ").append(contact.getName()).append("\n");
            content.append("Фамилия: ").append(contact.getLastName()).append("\n");
            content.append("Отчество: ").append(contact.getMiddleName()).append("\n");
            content.append("Телефоны: ").append("\n");
            for (String phoneNumber : contact.getPhoneNumbers()) {
                content.append("- ").append(phoneNumber).append("\n");
            }
            content.append("\n");
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private void showNoResultsMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Результаты поиска");
        alert.setHeaderText(null);
        alert.setContentText("Ничего не найдено");
        alert.showAndWait();
    }
    private void showAddContactDialog() {
        Dialog<Contact> dialog = new Dialog<>();
        dialog.setTitle("Добавить контакт");
        dialog.setHeaderText("Введите данные контакта");

        ButtonType addButton = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButton, ButtonType.CANCEL);

        TextField nameField = new TextField();
        TextField lastNameField = new TextField();
        TextField middleNameField = new TextField();

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.addRow(0, new Label("Имя:"), nameField);
        gridPane.addRow(1, new Label("Фамилия:"), lastNameField);
        gridPane.addRow(2, new Label("Отчество:"), middleNameField);

        List<TextField> phoneNumberFields = new ArrayList<>();
        List<ComboBox<String>> phoneTypeComboBoxes = new ArrayList<>();
        VBox phoneVBox = new VBox();

        Button addPhoneNumberButton = new Button("Добавить телефон");
        addPhoneNumberButton.setOnAction(e -> {
            TextField newPhoneNumberField = new TextField();
            ComboBox<String> newPhoneTypeComboBox = new ComboBox<>();
            newPhoneTypeComboBox.getItems().addAll("Сотовый", "Факс", "Домашний");
            newPhoneTypeComboBox.setValue("Сотовый");
            phoneNumberFields.add(newPhoneNumberField);
            phoneTypeComboBoxes.add(newPhoneTypeComboBox);
            phoneVBox.getChildren().add(new HBox(new Label("Телефон:"), newPhoneNumberField, newPhoneTypeComboBox));
            newPhoneNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    newPhoneNumberField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });
        });

        gridPane.add(addPhoneNumberButton, 1, 4);

        ScrollPane scrollPane = new ScrollPane();
        VBox content = new VBox();
        content.getChildren().addAll(gridPane, phoneVBox);
        content.setSpacing(10);
        scrollPane.setContent(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        dialog.getDialogPane().setContent(scrollPane);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == addButton) {
                String name = nameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                String middleName = middleNameField.getText().trim();
                List<String> phoneNumbers = new ArrayList<>();
                List<String> phoneTypes = new ArrayList<>();

                for (TextField newPhoneNumberField : phoneNumberFields) {
                    String phoneNumber = newPhoneNumberField.getText().trim();

                    if (!phoneNumber.isEmpty()) {
                        phoneNumbers.add(phoneNumber);
                    }
                }

                for (ComboBox<String> phoneTypeComboBox : phoneTypeComboBoxes) {
                    phoneTypes.add(phoneTypeComboBox.getValue()); // Get the selected phone type
                }

                if (!name.isEmpty() && !lastName.isEmpty() && !middleName.isEmpty() && !phoneNumbers.isEmpty()) {
                    Contact contact = new Contact(name);
                    contact.setLastName(lastName);
                    contact.setMiddleName(middleName);

                    for (int i = 0; i < phoneNumbers.size(); i++) {
                        String phone = phoneNumbers.get(i) + " (" + phoneTypes.get(i) + ")";
                        contact.addPhoneNumber(phone);
                    }

                    phoneBook.addContact(contact);
                    contactList.add(contact);
                }
            }

            return null;
        });

        dialog.showAndWait();
    }

    private PhoneBook loadPhoneBook() {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("phonebook.dat"))) {
            return (PhoneBook) inputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new PhoneBook();
        }
    }

    private void savePhoneBook(PhoneBook phoneBook) {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("phonebook.dat"))) {
            outputStream.writeObject(phoneBook);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}