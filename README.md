# tasker-keeper
Tasker Keeper is an in-progress Android app with a habit tracker, task list, calendar, and diary.

The tasks tab allows the user to create different lists of tasks. Within each list's page, tasks in a Room database can be created, deleted, and marked as complete or incomplete. The tasks can be placed in tiered levels of parent / child relationships, allowing minimization and expanding at any parent task. Long-pressing the drag indicator enables 2 drag modes. Starting the drag movement vertically moves the selected task and all of its subtasks to any other vertical position in the list, while enabling horizontal movement to any tier that maintains the parent / child relationships of the other tasks. Starting the drag movement horizontally maintains the selected task's vertical position in the list but enables changing its parent / child relationships with the below tasks, to any tier that doesn't leave a task without parents. The visual indication of the drag will show only the tiers that are possible, highlighting in red if the dragged task and its children can't fit at the selected vertical position due to exceeding the maximum number of tiers.

https://github.com/user-attachments/assets/47a73e2d-0c0c-42e3-8858-be6d16c70cf3

The in-progress habit tracker will have habits that will refresh daily, weekly, or monthly.

The in-progress calendar will show tasks that are set for specific dates, and clicking on them will take you to that date's task list in the tasks tab.

The in-progress diary will automatically create a daily entry that archives the habits and tasks completed on that date, as well as any notes typed up in the diary tab for that day.
