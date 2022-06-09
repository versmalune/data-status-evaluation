package kr.co.data_status_evaluation.config;

import kr.co.data_status_evaluation.model.Account;
import kr.co.data_status_evaluation.model.AccountAdapter;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class CustomMethodSecurityExpressionRoot
        extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private Object filterObject;
    private Object returnObject;
    private Object target;

    public CustomMethodSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
    }

    public boolean hasJob(String[] authors) {
        if (!(this.getPrincipal() instanceof AccountAdapter)) {
            return false;
        }
        Account account = ((AccountAdapter)this.getPrincipal()).getAccount();
        for(String author:authors) {
            if(account.hasAuthor(author)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    @Override
    public Object getThis() {
        return this;
    }
}
