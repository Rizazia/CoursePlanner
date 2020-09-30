# CoursePlanner

The purpose of this mobile application is to provide users a way to plan their courses and related activities in any university/college. Data permenence is handled through a SQLite database and is stored on the local device. Design layouts were done in XML while all functionality is done in Java. The rest of this document will describe what each Java activity/class does and how it is done.

# Activities
## MainActivity

The first activity that launches when the application starts. Displays a sumary of the user's current term/course (or lack thereof) as well as a button that allows access to the user's terms/courses/assessments/mentors that will open the respective activity that handle such object.

## CurrentEnrollmentActivity

Displays the current term and course the user is enrolled in. This is determined by refrencing the device's date and the start/end dates of the terms and courses within the database. If no term or course is found a message is displayed depicting that, else the term/course will be displayed (clicking it will open a activity with more detailed information). If the user is enrolled in a course, the course's mentor(s) and assessment(s) will be listed and clicking them will also open their information in an activity. The user is also provided buttons that will allow them to quickly add a new mentor or assessment.

## ItemListActivity

Displays a list of the user's terms, courses, mentors, or assessments (referred to as items) determined via polymorphism when the activity is started. Clicking any of the items will open the activity that described it in full and allows modification of that item in a new activity. There is also a button that allows for the creation of a new item that will open the activity that handles its creation (again determined through the same polymorphism as the rest of the activity).

## AddMod (Item) Activity
These are all simalar in function and only differ in the form used to describe the item. Polymorphism is NOT used to collapse these four activities as each requires a different form and combining the forms into one page would severly reduce readuce readability throughout the file and splitting the activity does not impact performance and file size in a meaningful way in this instance. Since they are all mostly the same they will be described at the same time.
### The Specific files described here are:
#### AddModAssessmentActivity
#### AddModCourseActivity
#### AddModMentorActivity
#### AddModTermActivity

These activities allow for the creation/alteration of an item. Whether moddification or creation is being conducted is decided by how the activity is called (If the user interacts with an existing item they're modding, else adding). When modding this also doubles as the detailed view of all of the item's information while still allowing the user to change any of the information the item contains. When submiting a new or altered item the activity will validate the input; all fields expect the note's field must contain something, any dates must make sense (start date must be before the end date, course dates must be within its assigned term's dates, etc.), and for any field that reference a different field, the other item must be created first. The user is provided quick access to creating fields that reference other items via floating action buttons that will return to the user back to the calling activity when completed. For any item that involves time (term, course, assessment) a notification togle exists that will alert the user on any start/end/due date via the devices notification system (default: no). If the user is modifying an item, a sharing button will usable that will allow the user to quickly take the item's information into the device's messaging applications.

## SearchActivity

Accessed through the magnifying glass in the toolbar (described later) and provides a very simple search feature. The search function only checks for items of a name matching a provided string. The entire database is queried to find matching anmes and results (or lack of) is displayed under the search bar and interacting with any results will open that items in its AddMod activity.

## OverviewActivity

Accessed through the drop menu in the toolbar (described later) and provides an overview of the values in the database as a whole. cliking on one the items and its count will display a list of all of the item  within the database. Clicking a specific item will bring it up in its AddModd activity.

# Fragments
## DatePickerDialogFragment

The only fragment used in the application. Used whenever a field that requires a date to bring up a calander that allows the user to select a date. When/if a date is selected, that date is formatted and returned to the calling activity. If the selection is canceled by closing the fragment, the old value is maintained (either the old date, or null which will fail validation).

# Classes
## ChannelHandler NotificationHelper & NotificationReceiver

These classes work together to accomplish a single task and as such will be explained togeether. Whenever a notification is created, it is sent through the reciever and managed by the helper that adds/cancels the notification to the channel decided in ChannelHandler.

## DBHelper

Handles all interaction with the SQLite database used in this application. Since this is a SQLite database and not a regular SQL database this class handles the creation of the database. All interactions with the database pass through this class. The structure of the database is described below.

# Database Structure

As described earlier, this application uses SQLite which provides offline access but prevents loading a specific institution's courses and information, which would be my desired outcome if I were further develop this application. And becaue of that the database is stored on the local device and defined within the application as follows.

# Term Table
### Fields:
ID - int PrimaryKey AutoIncrement

Name - string not null

StartDate - date not null

EndDate - date not null


# Course Table
### Fields:
ID - int PrimaryKey AutoIncrement

TermId - int references(Term) not null

MentorId - int references(Mentor) not null

Name - string not null

StartDate - date not null

EndDate - date not null

Status - string not null

Notes - string

Hasnotification - boolean not null default(false)


# Assessment table
### Fields:
ID - int PrimaryKey AutoIncrement

CourseId - int references(Course) not null

Name - string not null

Type - string not null

Notes - String



#### Note: the mentor table is split into three tables to allow an individual mentor to possess an arbitrary amount of phone numbers and email adrresses efficiently as it is not unheard of individuals to have multiple emails/phone numbers 
# Mentor table
### Fields:
ID - int PrimaryKey AutoIncrement

Name - string not null

# MentorPhone table
ID - int PrimaryKey AutoIncrement

MentorId - int refrences(Mentor) not null

PhoneNumber - string not null


# MentorEmail Table
ID - int PrimaryKey AutoIncrement

MentorId - int refrences(Mentor) not null

email - string not null


# Notification table
Note: this table is used for the application to find specific notifications post-creation

ID - int PrimaryKey AutoIncrement

TableID - refrences(Id of any other table) not null

TableName - string not null (note: refrences the name another table)

Name - string not null



Please feel free to contact me with any questions on this applications.
