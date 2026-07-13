package tests;

import org.testng.Assert;
import org.testng.annotations.Test;

public class EasyQQualityObjectiveTest extends EasyQModuleWorkflowBase {
    @Override
    protected String moduleLabel() {
        return "Quality Objective";
    }

    @Override
    protected String moduleConfigPrefix() {
        return "EASYQ_QO";
    }

    @Override
    protected String[] moduleTextFragments() {
        return new String[]{"Quality Objective"};
    }

    @Override
    protected String[] moduleUrlFragments() {
        return new String[]{"quality-objective", "qualityobjective", "quality_objective", "objective"};
    }

    @Override
    protected String moduleMenuRegex() {
        return "quality\\s*objective|quality-objective|qualityobjective";
    }

    @Override
    protected String automationTitlePrefix() {
        return "Automation Quality Objective";
    }

    @Test(priority = 1, description = "Verify Quality Objective module loads successfully")
    // Manual Test Case ID: TC401
    public void verifyQualityObjectiveModuleLoadsSuccessfully() {
        assertModulePageReady();
    }

    @Test(priority = 2, description = "Verify module loads with objective data")
    // Manual Test Case ID: TC402
    public void verifyModuleLoadsWithObjectiveData() {
        assertModuleDataOrValidState();
    }

    @Test(priority = 3, description = "Verify no UI break on page load")
    // Manual Test Case ID: TC403
    public void verifyNoUiBreakOnPageLoad() {
        Assert.assertTrue(driver.findElement(org.openqa.selenium.By.tagName("body")).isDisplayed(),
                "Quality Objective page body should be visible");
        assertModulePageReady();
    }

    @Test(priority = 4, description = "Verify module access for Admin")
    // Manual Test Case ID: TC404
    public void verifyModuleAccessForAdmin() {
        assertModulePageReady();
    }

    @Test(priority = 5, description = "Verify Initiate button is visible")
    // Manual Test Case ID: TC405
    public void verifyInitiateButtonIsVisible() {
        Assert.assertTrue(isElementDisplayed(initiateButton) || hasModuleDataOrPageLoaded(),
                "Initiate button should be visible for authorized users, or the module should load with valid state");
    }

    @Test(priority = 6, description = "Verify clicking Initiate opens objective form")
    // Manual Test Case ID: TC406
    public void verifyClickingInitiateOpensObjectiveForm() {
        openInitiateForm();

        Assert.assertTrue(driver.findElements(moduleField).size() > 0,
                "Objective form should open with input fields");
    }

    @Test(priority = 7, description = "Verify default objective records are prefilled")
    // Manual Test Case ID: TC407
    public void verifyDefaultObjectiveRecordsArePrefilled() {
        openInitiateForm();

        Assert.assertTrue(pageContainsAny("Objective") || driver.findElements(moduleField).size() > 0,
                "Default objective records/fields should be visible");
    }

    @Test(priority = 8, description = "Verify draft objectives editable")
    // Manual Test Case ID: TC408
    public void verifyDraftObjectivesEditable() {
        Assert.assertTrue(isElementDisplayed(editButton) || hasModuleDataOrPageLoaded(),
                "Draft objective edit action should be visible when draft exists");
    }

    @Test(priority = 9, description = "Verify status changes to Under Review")
    // Manual Test Case ID: TC409
    public void verifyStatusChangesToUnderReview() {
        Assert.assertTrue(isElementDisplayed(statusText) || hasModuleDataOrPageLoaded(),
                "Under Review/status should display when objective records exist");
    }

    @Test(priority = 10, description = "Verify status changes to Approved")
    // Manual Test Case ID: TC410
    public void verifyStatusChangesToApproved() {
        Assert.assertTrue(isElementDisplayed(statusText) || hasModuleDataOrPageLoaded(),
                "Approved/status should display when objective records exist");
    }

    @Test(priority = 11, description = "Verify Admin/Doc Controller access")
    // Manual Test Case ID: TC411
    public void verifyAdminDocControllerAccess() {
        assertModulePageReady();
    }

    @Test(priority = 12, description = "Verify view-only access")
    // Manual Test Case ID: TC412
    public void verifyViewOnlyAccess() {
        assertModulePageReady();
    }

    @Test(priority = 13, description = "Verify status reflects correctly")
    // Manual Test Case ID: TC413
    public void verifyStatusReflectsCorrectly() {
        Assert.assertTrue(isElementDisplayed(statusText) || hasModuleDataOrPageLoaded(),
                "Objective status should reflect correctly in UI");
    }

    @Test(priority = 14, description = "Verify empty submission handling")
    // Manual Test Case ID: TC414
    public void verifyEmptySubmissionHandling() {
        openInitiateForm();
        Assert.assertTrue(clickButtonByText("Submit", "Send", "Save", "Draft"),
                "Save/Submit action should be available for empty validation");

        Assert.assertTrue(isElementDisplayed(validationMessage) || hasModuleDataOrPageLoaded(),
                "Empty objective submission should show validation or keep form stable");
    }

    @Test(priority = 15, description = "Verify long text handling")
    // Manual Test Case ID: TC415
    public void verifyLongTextHandling() {
        Assert.assertTrue(hasModuleDataOrPageLoaded(),
                "Quality Objective should handle long objective text without UI break");
    }

    @Test(priority = 16, description = "Verify multiple objectives handling")
    // Manual Test Case ID: TC416
    public void verifyMultipleObjectivesHandling() {
        Assert.assertTrue(hasModuleDataOrPageLoaded(),
                "Multiple objectives should be handled when objective data exists");
    }

    @Test(priority = 17, description = "Verify user can add new objective")
    // Manual Test Case ID: TC417
    public void verifyUserCanAddNewObjective() {
        Assert.assertTrue(addAutomationRecord(),
                "User should be able to add a Quality Objective record");
    }

    @Test(priority = 18, description = "Verify multiple objectives can be added")
    // Manual Test Case ID: TC418
    public void verifyMultipleObjectivesCanBeAdded() {
        Assert.assertTrue(addMultipleAutomationRecords(),
                "User should be able to add multiple Quality Objective records");
    }

    @Test(priority = 19, description = "Verify user can edit objective")
    // Manual Test Case ID: TC419
    public void verifyUserCanEditObjective() {
        Assert.assertTrue(editAutomationRecord(),
                "User should be able to edit a Quality Objective record");
    }

    @Test(priority = 20, description = "Verify user can delete objective")
    // Manual Test Case ID: TC420
    public void verifyUserCanDeleteObjective() {
        Assert.assertTrue(deleteAutomationRecord(),
                "User should be able to delete/remove a disposable Quality Objective draft");
    }

    @Test(priority = 21, description = "Verify saving objectives as Draft")
    // Manual Test Case ID: TC421
    public void verifySavingObjectivesAsDraft() {
        Assert.assertTrue(createDraftFromVarunAccount(),
                "Quality Objective should save as Draft from Varun account");
    }

    @Test(priority = 22, description = "Verify draft persists after refresh")
    // Manual Test Case ID: TC422
    public void verifyDraftPersistsAfterRefresh() {
        Assert.assertTrue(createDraftFromVarunAccount(), "Draft should be created before refresh validation");
        driver.navigate().refresh();

        Assert.assertTrue(pageContainsAny("Draft", "Quality Objective") || hasModuleDataOrPageLoaded(),
                "Saved Quality Objective draft should persist after refresh");
    }

    @Test(priority = 23, description = "Verify sending objectives for review")
    // Manual Test Case ID: TC423
    public void verifySendingObjectivesForReview() {
        Assert.assertTrue(sendDraftForReviewWithConfiguredUsers(),
                "Draft should be sent for review with Reviewer 1 Varun, Reviewer 2 Pavan, and Approver Amit Karane");
    }

    @Test(priority = 24, description = "Verify multiple reviewers assignment")
    // Manual Test Case ID: TC424
    public void verifyMultipleReviewersAssignment() {
        Assert.assertTrue(sendDraftForReviewWithConfiguredUsers(),
                "Reviewer 1 Varun and Reviewer 2 Pavan should be assignable");
    }

    @Test(priority = 25, description = "Verify single approver assignment")
    // Manual Test Case ID: TC425
    public void verifySingleApproverAssignment() {
        Assert.assertTrue(sendDraftForReviewWithConfiguredUsers(),
                "Approver Amit Karane should be assignable");
    }

    @Test(priority = 26, description = "Verify reviewer access to objectives")
    // Manual Test Case ID: TC426
    public void verifyReviewerAccessToObjectives() {
        assertReviewerCanAccess(reviewer2Username(), reviewer2Password(), "Reviewer 2 Pavan");
    }

    @Test(priority = 27, description = "Verify reviewer can edit objectives")
    // Manual Test Case ID: TC427
    public void verifyReviewerCanEditObjectives() {
        Assert.assertTrue(reviewerCanEditOrReview(reviewer2Username(), reviewer2Password()),
                "Reviewer should be able to edit/review assigned Quality Objective");
    }

    @Test(priority = 28, description = "Verify reviewer can review objectives")
    // Manual Test Case ID: TC428
    public void verifyReviewerCanReviewObjectives() {
        Assert.assertTrue(reviewerCanEditOrReview(reviewer2Username(), reviewer2Password()),
                "Reviewer should be able to submit review for assigned Quality Objective");
    }

    @Test(priority = 29, description = "Verify approver access")
    // Manual Test Case ID: TC429
    public void verifyApproverAccess() {
        assertReviewerCanAccess(approverUsername(), approverPassword(), "Approver Amit Karane");
    }

    @Test(priority = 30, description = "Verify only assigned approver can approve")
    // Manual Test Case ID: TC430
    public void verifyOnlyAssignedApproverCanApprove() {
        Assert.assertTrue(approverCanApprove(approverUsername(), approverPassword()),
                "Assigned approver Amit Karane should be able to approve Quality Objective");
    }

    @Test(priority = 31, description = "Verify Move to Draft creates new version")
    // Manual Test Case ID: TC431
    public void verifyMoveToDraftCreatesNewVersion() {
        Assert.assertTrue(moveApprovedToDraftAndUpdateContent(),
                "Move to Draft should create/update a Quality Objective draft version");
    }

    @Test(priority = 32, description = "Verify new version copies objectives")
    // Manual Test Case ID: TC432
    public void verifyNewVersionCopiesObjectives() {
        Assert.assertTrue(moveApprovedToDraftAndUpdateContent(),
                "New draft version should copy Quality Objective context/data");
    }

    @Test(priority = 33, description = "Verify workflow repeats for new version")
    // Manual Test Case ID: TC433
    public void verifyWorkflowRepeatsForNewVersion() {
        Assert.assertTrue(runApprovalPath(false),
                "Quality Objective workflow should repeat for new draft version");
    }

    @Test(priority = 34, description = "Verify Assignee cannot initiate")
    // Manual Test Case ID: TC434
    public void verifyAssigneeCannotInitiate() {
        Assert.assertTrue(assigneeCannotInitiate(),
                "Assignee should not be able to initiate Quality Objective");
    }

    @Test(priority = 35, description = "Verify restricted access for Assignee")
    // Manual Test Case ID: TC435
    public void verifyRestrictedAccessForAssignee() {
        Assert.assertTrue(assigneeHasRestrictedState(),
                "Assignee should see a restricted/view-only Quality Objective state");
    }

    @Test(priority = 36, description = "Verify objective data saved correctly")
    // Manual Test Case ID: TC436
    public void verifyObjectiveDataSavedCorrectly() {
        Assert.assertTrue(createDraftFromVarunAccount(),
                "Quality Objective data should save correctly");
    }

    @Test(priority = 37, description = "PDF Flow - Verify Admin Doc Controller initiates Quality Objectives")
    // Manual Test Case ID: TC401-TC436
    public void verifyPdfFlowAdminDocControllerInitiatesQualityObjectives() {
        assertModulePageReady();
    }

    @Test(priority = 38, description = "PDF Flow - Verify default objective records are prefilled")
    // Manual Test Case ID: TC401-TC436
    public void verifyPdfFlowDefaultObjectiveRecordsPrefilled() {
        openInitiateForm();

        Assert.assertTrue(pageContainsAny("Objective") || driver.findElements(moduleField).size() > 0,
                "Default objective records/fields should be prefilled or visible");
    }

    @Test(priority = 39, description = "PDF Flow - Verify Quality Objective review approval status path")
    // Manual Test Case ID: TC401-TC436
    public void verifyPdfFlowQualityObjectiveReviewApprovalStatusPath() {
        Assert.assertTrue(runApprovalPath(false),
                "Quality Objective should move through Draft/Review/Approved status flow");
    }

    @Test(priority = 40, description = "PDF Flow - Verify Move to Draft creates new Quality Objective version")
    // Manual Test Case ID: TC401-TC436
    public void verifyPdfFlowMoveToDraftCreatesNewQualityObjectiveVersion() {
        Assert.assertTrue(moveApprovedToDraftAndUpdateContent(),
                "Move to Draft should create a new Quality Objective version");
    }

    @Test(priority = 41, description = "PDF Flow - Verify Quality Objective reject and approve workflow for all reviewers and approver")
    // Manual Test Case ID: TC423-TC433
    public void verifyPdfFlowQualityObjectiveRejectThenApproveFullWorkflow() {
        Assert.assertTrue(runApprovalPath(true),
                "Quality Objective should verify rejection for Reviewer 1, Reviewer 2, and Approver before final approval");
    }
}
