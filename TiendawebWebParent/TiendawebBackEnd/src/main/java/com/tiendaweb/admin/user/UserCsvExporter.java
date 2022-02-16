package com.tiendaweb.admin.user;

import com.tiendaweb.common.entity.User;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UserCsvExporter {

    private static final String[] HEADERS = {"User ID", "E-mail", "First Name", "Last Name", "Roles", "Enabled"};
    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.withHeader(HEADERS);

    public void export(List<User> listUsers, HttpServletResponse response) throws IOException {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String timestamp = dateFormatter.format(new Date());
        String filename = "users_" + timestamp + ".csv";

        response.setContentType("text/csv");

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + filename;
        response.setHeader(headerKey, headerValue);

        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(),
                CsvPreference.STANDARD_PREFERENCE);

        String[] csvHeader = {"User ID", "E-mail", "First Name", "Last Name", "Roles", "Enabled"};
        String[] fieldMapping = { "id", "email", "firstName", "lastName", "roles", "enabled"};

        csvWriter.writeHeader(csvHeader);

        for (User user : listUsers) {
            csvWriter.write(user, fieldMapping);
        }

        csvWriter.close();
    }


    //load data into csv
    public ByteArrayInputStream load(final List<User> users) {
        return writeDataToCsv(users);
    }

    //write data to csv
    private ByteArrayInputStream writeDataToCsv(final List<User> users) {
        //log.info("Writing data to the csv printer");
        try (final ByteArrayOutputStream stream = new ByteArrayOutputStream();
             final CSVPrinter printer = new CSVPrinter(new PrintWriter(stream), FORMAT)) {
            for (final User user : users) {
                final List<String> data = Arrays.asList(
                        String.valueOf(user.getId()),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRoles().toString(),
                        String.valueOf(user.isEnabled()));

                printer.printRecord(data);
            }

            printer.flush();
            return new ByteArrayInputStream(stream.toByteArray());
        } catch (final IOException e) {
            throw new RuntimeException("Csv writing error: " + e.getMessage());
        }
    }
}
