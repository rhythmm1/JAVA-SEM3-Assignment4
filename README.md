# City Library Digital Management System

A command-line interface (CLI) application for managing library operations including book inventory, member management, and book issuing/returning functionality.

[![Run on OnlineGDB](https://img.shields.io/badge/Run%20on-OnlineGDB-blue?logo=java&style=for-the-badge)](https://onlinegdb.com/tma3zRg5wV)

## Features

- **Book Management**: Add, search, and sort books by title, author, or category
- **Member Management**: Register library members with email validation
- **Issue/Return System**: Track book lending and returns
- **Persistent Storage**: Data is saved to local files (books.txt, members.txt)
- **Search Functionality**: Find books by title, author, or category
- **Sorting Options**: Sort books by title, author, or category

## Requirements

- Java 11 or higher
- Standard Java libraries (no external dependencies)

## Installation

1. Clone the repository:
```bash
git clone https://github.com/abhinavgautam08/City-Library-Digital-Management-System.git
cd City-Library-Digital-Management-System
```

2. Compile the program:
```bash
javac Main.java
```

3. Run the application:
```bash
java Main
```

## Usage

When you run the program, you'll see a menu with the following options:

1. **Add Book** - Add a new book to the library inventory
2. **Add Member** - Register a new library member
3. **Issue Book** - Issue a book to a member
4. **Return Book** - Process a book return
5. **Search Books** - Search for books by title, author, or category
6. **Sort Books** - Display books sorted by title, author, or category
7. **Exit** - Save data and exit the program

### Example Workflow

```
1. Add a book:
   - Title: The Great Gatsby
   - Author: F. Scott Fitzgerald
   - Category: Fiction

2. Add a member:
   - Name: John Doe
   - Email: john.doe@example.com

3. Issue a book:
   - Enter the Book ID
   - Enter the Member ID

4. Return a book:
   - Enter the Book ID
   - Enter the Member ID
```

## Data Storage

The application stores data in two text files:
- `books.txt` - Contains all book records
- `members.txt` - Contains all member records

Data is automatically loaded when the program starts and saved when you exit.

## Data Format

### Books
Books are stored with the following information:
- Book ID (auto-generated)
- Title
- Author
- Category
- Issue status (available/issued)

### Members
Members are stored with:
- Member ID (auto-generated)
- Name
- Email
- List of currently issued books# City-Library-Digital-Management-System
