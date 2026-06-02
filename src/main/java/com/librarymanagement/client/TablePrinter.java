package com.librarymanagement.client;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Utility class to print data in table format to console
 */
public class TablePrinter {

    public static void printBooks(List<?> books) {
        if (books == null || books.isEmpty()) {
            System.out.println("No data found.");
            return;
        }

        String[] headers = {
                "ID", "Title", "Author", "Genre", "Copies", "Borrowed"
        };

        int[] widths = {
                12, 30, 25, 15, 10, 10
        };

        printTableHeader(headers, widths);

        for (Object obj : books) {
            printRow(getBookRow(obj), widths);
        }

        printTableFooter(widths, books.size());
    }

    public static void printMembers(List<?> members) {
        if (members == null || members.isEmpty()) {
            System.out.println("No data found.");
            return;
        }

        String[] headers = {
                "ID", "Name", "Email", "Phone", "Join Date"
        };

        int[] widths = {
                12, 25, 35, 15, 15
        };

        printTableHeader(headers, widths);

        for (Object obj : members) {
            printRow(getMemberRow(obj), widths);
        }

        printTableFooter(widths, members.size());
    }

    public static void printBorrowRecords(List<?> records) {
        if (records == null || records.isEmpty()) {
            System.out.println("No data found.");
            return;
        }

        String[] headers = {
                "Record ID",
                "Book ID",
                "Book Title",
                "Member ID",
                "Member Name",
                "Borrow Date",
                "Due Date",
                "Return Date",
                "Status"
        };

        int[] widths = {
                12,
                12,
                25,
                12,
                20,
                12,
                12,
                12,
                12
        };

        printTableHeader(headers, widths);

        for (Object obj : records) {
            printRow(getBorrowRecordRow(obj), widths);
        }

        printTableFooter(widths, records.size());
    }

    private static void printTableHeader(String[] headers, int[] widths) {
        printLine(widths);
        printRow(headers, widths);
        printLine(widths);
    }

    private static void printTableFooter(int[] widths, int totalRecords) {
        printLine(widths);
        System.out.println("Total: " + totalRecords + " records");
    }

    private static String[] getBookRow(Object obj) {
        try {
            Method getId = obj.getClass().getMethod("getId");
            Method getTitle = obj.getClass().getMethod("getTitle");
            Method getAuthor = obj.getClass().getMethod("getAuthor");
            Method getGenre = obj.getClass().getMethod("getGenre");
            Method getCopies = obj.getClass().getMethod("getCopies");
            Method getBorrowed = obj.getClass().getMethod("getBorrowed");

            return new String[]{
                    safe(getId.invoke(obj)),
                    safe(getTitle.invoke(obj)),
                    safe(getAuthor.invoke(obj)),
                    safe(getGenre.invoke(obj)),
                    safe(getCopies.invoke(obj)),
                    safe(getBorrowed.invoke(obj))
            };
        } catch (Exception e) {
            return errorRow(6);
        }
    }

    private static String[] getMemberRow(Object obj) {
        try {
            Method getId = obj.getClass().getMethod("getId");
            Method getName = obj.getClass().getMethod("getName");
            Method getEmail = obj.getClass().getMethod("getEmail");
            Method getPhone = obj.getClass().getMethod("getPhone");
            Method getJoinedDate = obj.getClass().getMethod("getJoinedDate");

            return new String[]{
                    safe(getId.invoke(obj)),
                    safe(getName.invoke(obj)),
                    safe(getEmail.invoke(obj)),
                    safe(getPhone.invoke(obj)),
                    safe(getJoinedDate.invoke(obj))
            };
        } catch (Exception e) {
            return errorRow(5);
        }
    }

    private static String[] getBorrowRecordRow(Object obj) {
        try {
            Method getId = obj.getClass().getMethod("getId");
            Method getBookId = obj.getClass().getMethod("getBookId");
            Method getBookTitle = obj.getClass().getMethod("getBookTitle");
            Method getMemberId = obj.getClass().getMethod("getMemberId");
            Method getMemberName = obj.getClass().getMethod("getMemberName");
            Method getBorrowDate = obj.getClass().getMethod("getBorrowDate");
            Method getDueDate = obj.getClass().getMethod("getDueDate");
            Method getReturnDate = obj.getClass().getMethod("getReturnDate");
            Method isReturned = obj.getClass().getMethod("isReturned");

            String status =
                    Boolean.TRUE.equals(isReturned.invoke(obj))
                            ? "RETURNED"
                            : "BORROWED";

            return new String[]{
                    safe(getId.invoke(obj)),
                    safe(getBookId.invoke(obj)),
                    safe(getBookTitle.invoke(obj)),
                    safe(getMemberId.invoke(obj)),
                    safe(getMemberName.invoke(obj)),
                    dateToString(getBorrowDate.invoke(obj)),
                    dateToString(getDueDate.invoke(obj)),
                    dateToString(getReturnDate.invoke(obj)),
                    status
            };
        } catch (Exception e) {
            return errorRow(9);
        }
    }

    private static void printRow(String[] values, int[] widths) {
        StringBuilder sb = new StringBuilder("|");

        for (int i = 0; i < values.length; i++) {
            sb.append(formatCell(values[i], widths[i]));
        }

        System.out.println(sb);
    }

    private static String formatCell(String value, int width) {
        String text = truncate(value, width);
        return String.format(" %-" + width + "s |", text);
    }

    private static void printLine(int[] widths) {
        StringBuilder sb = new StringBuilder("+");

        for (int width : widths) {
            sb.append("-".repeat(width + 2));
            sb.append("+");
        }

        System.out.println(sb);
    }

    private static String truncate(String str, int maxLength) {
        if (str == null) {
            return "N/A";
        }

        if (str.length() <= maxLength) {
            return str;
        }

        return str.substring(0, maxLength - 3) + "...";
    }

    private static String dateToString(Object date) {
        if (date == null) {
            return "N/A";
        }

        String text = date.toString();

        return text.length() >= 10
                ? text.substring(0, 10)
                : text;
    }

    private static String safe(Object value) {
        return value == null ? "N/A" : value.toString();
    }

    private static String[] errorRow(int size) {
        String[] row = new String[size];

        for (int i = 0; i < size; i++) {
            row[i] = "N/A";
        }

        return row;
    }
}