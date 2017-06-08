package ch.ethz.sae;

import java.util.List;

import soot.Value;
import apron.*;

// Wrapper of an abstract element
public class AWrapper {

    private Abstract1 elem;
    Manager man;

    public AWrapper(Abstract1 e) {
        elem = e;
    }

    public Abstract1 get() {
        return elem;
    }

    public void set(Abstract1 e) {
        elem = e;
    }

    public void copy(AWrapper src) {
        this.elem = src.get();
    }

    public boolean equals(Object o) {
        Abstract1 t = ((AWrapper) o).get();
        try {
            if (elem.isEqual(man, t) != elem.isIncluded(man, t))
                System.out.println("VIOLA");
            return elem.isIncluded(man, t);
        } catch (ApronException e) {
            System.err.println("isEqual failed");
            System.exit(-1);
        }
        return false;
    }

    public String toString() {
        try {
            if (elem.isTop(man))
                return "<Top>";

            return elem.toString();
        } catch (ApronException e) {
            System.err.println("toString failed");
            System.exit(-1);
        }
        return null;
    }
	
	// checks if the arguments to weldBetween calls overlap (if applicable)
	boolean doArgsOverlap(List<Value> args) throws ApronException {
		// only check for weldBetween
		if (args.size() == 2 && !isLessThan(args.get(0), args.get(1), true)) {
			Logger.logConstraintViolation("weldBetween arguments might overlap!");
			Logger.logIndenting(4, elem);
			return true;
		}
		return false;
	}
	
	// convenient way of calling isLessThan(Texpr1Node…) with unparsed Soot Values
	boolean isLessThan(Value l, Value r, boolean strict) throws ApronException {
		Texpr1Node lhs = Analysis.toExpr(l);
		Texpr1Node rhs = Analysis.toExpr(r);
		return isLessThan(lhs, rhs, strict);
	}
	
	// checks if l is guaranteed to be less than r in the given state
	boolean isLessThan(Texpr1Node l, Texpr1Node r, boolean strict) throws ApronException {
		Texpr1Node sub = new Texpr1BinNode(Texpr1BinNode.OP_SUB, r, l);
		int op = strict ? Tcons1.SUP : Tcons1.SUPEQ;
		return satisfy(new Tcons1(elem.getEnvironment(), op, sub));
	}
	
	// because Abstract1.satisfy is imprecise
	boolean satisfy(Tcons1 constraint) throws ApronException {
		// bottom satisfies everything
		return elem.isBottom(man) || !elem.meetCopy(man, constraint).isBottom(man);
	}
	
	// returns an interval approximating all possible values of the given expression
	Interval possibleValuesOf(Texpr1Node expr) throws ApronException {
		Texpr1Intern inContext = new Texpr1Intern(elem.getEnvironment(), expr);
		return elem.getBound(man, inContext);
	}
}
