/* Generated By:JJTree: Do not edit this line. ASTConstant.java */

package uchicago.src.sim.parameter.rpl;

public class ASTConstant extends SimpleNode {

  public String paramName;
  public RPLObject value;
  public RPLParameter constant = null;

  public ASTConstant(int id) {
    super(id);
  }

  public ASTConstant(RPLParser p, int id) {
    super(p, id);
  }

  public void preProcess(RPLCompiler compiler) {
    paramName = children[0].getInfo().toString();
    compiler.addConstantName(paramName);
    value = children[1].getValue();
  }

  public void compile(RPLCompiler compiler) {
    value.compile(compiler);
    constant = RPLFactory.createConstant(paramName, value);
  }

  public String getName() {
    return paramName;
  }

  public RPLParameter getRPLConstant() {
    return constant;
  }
}
