package org.suai.persistence;

public class ASTPojo {

    private String studentName;
    private Integer labNumber;
    private String jsonAST;
    private String sourceCode;

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Integer getLabNumber() {
        return labNumber;
    }

    public void setLabNumber(Integer labNumber) {
        this.labNumber = labNumber;
    }

    public String getJsonAST() {
        return jsonAST;
    }

    public void setJsonAST(String jsonAST) {
        this.jsonAST = jsonAST;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
}
