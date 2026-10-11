# Lodge Billing Lab

A small JavaFX desktop application that calculates and records guest bills for a lodge.
Built for *ICT261 Lecture 3* at *Mulungushi University* (lecturer: Mr E Nyirenda).

The whole program is a single file, src/LodgeBillingLab.java. It uses no FXML, no database and no external libraries other than JavaFX itself.

## Features

- Enter guest name, room type, number of nights, extras, amount paid and optional notes
- *Calculate* shows the total and balance without saving
- *Save bill* adds the bill to the "Saved bills" table (bill numbers B001, B002, ...)
- *View receipt* shows a full breakdown of the selected bill
- *Delete selected bill* removes a bill after confirmation
- *New guest* clears the form
- Input validation with clear error messages (blank name, non-numeric nights, negative amounts, etc.)

## Billing rules

| Room type | Price per night |
|-----------|-----------------|
| Standard  | K 350.00        |
| Deluxe    | K 550.00        |
| Family    | K 750.00        |

- Stays of *5 nights or more* get *5% off the room charge*
- Extras get no discount
- No tax is applied
- Saved bills are kept in memory only and disappear when the app is closed


Room charge = price per night x nights
Discount    = 5% of room charge (only if nights >= 5)
Total       = room charge - discount + extras
Balance     = total - amount paid


Example: Deluxe, 5 nights, extras K 200, paid K 1000
-> room charge 2750.00, discount 137.50, total *K 2812.50, balance **K 1812.50*.

## Requirements

- JDK 21
- JavaFX SDK 21 (download from https://gluonhq.com/products/javafx/)
- IntelliJ IDEA (or any Java IDE)

## How to run in IntelliJ IDEA

1. Clone or download this repository and open the folder in IntelliJ.
2. Go to *File -> Project Structure -> Libraries -> + -> Java* and select the lib folder of your JavaFX SDK (for example C:\javafx\javafx-sdk-21.0.12\lib).
3. Go to *File -> Project Structure -> Project* and set the SDK to JDK 21.
4. Open *Run -> Edit Configurations, create an **Application* configuration with main class LodgeBillingLab.
5. Click *Modify options -> Add VM options* and paste (change the path to match your computer):


--module-path "C:\javafx\javafx-sdk-21.0.12\lib" --add-modules javafx.controls


6. Click *Apply, then **OK, then press the green **Run* button.

## How to run from the command line

From the project folder (change the JavaFX path to match your computer):


javac --module-path "C:\javafx\javafx-sdk-21.0.12\lib" --add-modules javafx.controls -d out src\LodgeBillingLab.java
java --module-path "C:\javafx\javafx-sdk-21.0.12\lib" --add-modules javafx.controls -cp out LodgeBillingLab


## Project structure


LodgeBillingLab/
|-- src/
|   `-- LodgeBillingLab.java   (all application code)
|-- README.md
`-- .gitignore


## Sample test cases

| Guest | Room | Nights | Extras | Paid | Expected result |
|-------|------|--------|--------|------|-----------------|
| Mary Banda | Standard | 2 | 100 | 500 | Total K 800.00, balance K 300.00 |
| John Phiri | Deluxe | 5 | 200 | 1000 | Total K 2812.50, balance K 1812.50 |
| (blank) | Deluxe | 3 | | | Error: please enter the guest name |
| Any | Family | abc | | | Error: nights must be a whole number |

## Assumptions and limitations

- Blank Extras and Amount paid fields are treated as 0
- Bill numbers are never reused after a bill is deleted
- Bills are not saved to disk or a database, so they are lost when the app closes
- Tested by running it in IntelliJ IDEA with JDK 21 and JavaFX 21.0.1Second-year Computer Science student, Mulungushi University, Zambia
- ## Author
-        FELIX NKAKA	202505808	COMPUTER SCIENCE
-       STEPHANIE CHANDA	202510778	COMPUTER SCIENCE
  		REGINA NABIYE	202501765	COMPUTER SCIENCE
  		MARCUS NGOMA	202502708	INFORMATION TECHNOLOGY
  		ISAAC MITI	202509238	COMPUTER SCIENCE
  		ISAAC CHIKANDILA 202512191	COMPUTER SCIENCE
  		BLESSING TEMBO	202501581	INFORMATION TECHNOLOGY
  		LAWRENCE KOLO	202502125	COMPUTER SCIENCE
  		LAYTON MUTEBA	202510831	COMPUTER SCIENCE
  		LUCIE BANDA	202500006	COMPUTER SCIENCE
  		DICKSON CHIBULU	202500500	COMPUTER SCIENCE
     	ISAAC MVULA	202507408	COMPUTER SCIENCE