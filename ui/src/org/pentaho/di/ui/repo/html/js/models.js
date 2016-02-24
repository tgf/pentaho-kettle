repoConnectionApp.service("repoModel", function() {
  this.repositories = [
    {
      "id": 1,
      "index": "1",
      "text": "Pentaho Repository",
      "message": "Enterprise ready storage designed for your business needs.<br /> The Pentaho Repository uses Apache Jackrabbit to store ETL metadata.",
      "link": "pentaho-repository",
      "class": "pentaho"
    },
    {
      "id": 2,
      "index:": "0",
      "text": "Kettle Database Repository",
      "message": "A Kettle Database Repository uses a central relational database<br /> to store ETL metadata.",
      "link": "kettle-database-repository",
      "class": "kettle-db"
    },
    {
      "id": 3,
      "index": "2",
      "text": "Kettle File Repository",
      "message": "A Kettle file repository uses your local file system<br /> to store ETL metadata.",
      "link": "kettle-file-repository",
      "class": "kettle-file"
    }
  ]
  this.selectedRepository = this.repositories[0];
});


repoConnectionApp.service("pentahoRepositoryModel",function() {
  this.displayName = "";
  this.url = "http://localhost:8080/pentaho-di";
  this.description = "";
});
