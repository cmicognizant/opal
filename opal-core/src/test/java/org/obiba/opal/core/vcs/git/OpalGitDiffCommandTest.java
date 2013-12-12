    OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(vcs.getRepository(DATASOURCE_NAME), COMMIT_ID)
        .addDatasourceName(DATASOURCE_NAME).build();
    List<String> diffs = command.execute();
    assertThat(diffs, matches(DIFF_VARIABLE));
    assertThat(diffs, matches(DIFF_VIEW));
    OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(vcs.getRepository(DATASOURCE_NAME), COMMIT_ID)
        .addPath("TestView/TOTO_VAR.js").addDatasourceName(DATASOURCE_NAME).build();
    List<String> diffs = command.execute();
    assertThat(diffs, matches(DIFF_VARIABLE));
    OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(vcs.getRepository(DATASOURCE_NAME), COMMIT_ID)
        .addPath("TestView").addDatasourceName(DATASOURCE_NAME).build();
    List<String> diffs = command.execute();
    assertThat(diffs, matches(DIFF_VARIABLE));
    assertThat(diffs, matches(DIFF_VIEW));
    new OpalGitDiffCommand.Builder(vcs.getRepository(DATASOURCE_NAME), COMMIT_ID).addDatasourceName(DATASOURCE_NAME)
        .addNthCommit(0).build().execute();
    OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(vcs.getRepository(DATASOURCE_NAME), COMMIT_ID)
        .addPath("TestView").addDatasourceName(DATASOURCE_NAME).addNthCommit(2).build();
    List<String> diffs = command.execute();
    assertThat(diffs, matches(DIFF_VIEW_TWO_VERSIONS_BACK));
    OpalGitDiffCommand command = new OpalGitDiffCommand.Builder(vcs.getRepository(DATASOURCE_NAME), "HEAD")
        .addPath("TestView/View.xml").addDatasourceName(DATASOURCE_NAME)
        .addPreviousCommitId("be77432d15dec81b4c60ed858d5d678ceb247171").build();
    List<String> diffs = command.execute();
    assertThat(diffs, matches(DIFF_VIEW_WITH_HEAD));
    List<String> diffs = vcs
        .getDiffEntries(DATASOURCE_NAME, "HEAD", "be77432d15dec81b4c60ed858d5d678ceb247171", "TestView/View.xml");
    assertThat(diffs, matches(DIFF_VIEW_WITH_HEAD));
  private static Matcher<List<String>> matches(final String expected) {
        return diffObject instanceof List ? matchDiffs((List<String>) diffObject) : diffObject.equals(theExpected);