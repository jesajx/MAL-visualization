//Generates JSON for the MAL-Visualization program

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;

public class Generator {

    // TODO add option to generate attack graph

    public static void generate(Lang lang, Map<String, String> args) throws CompilerException, FileNotFoundException, IOException {
        new Generator(lang, args);
    }

    class Con {
        public Lang.AttackStep step;
        public AttackStepField reach;

        public Con(Lang.AttackStep step, AttackStepField reach) {
            this.step = step;
            this.reach = reach;
        }
    }

    private String stepShape(Lang.AttackStep step) {
        switch (step.getType()) {
            case ALL: return "box";
            case ANY: return "ellipse";
            case DEFENSE: return "hexagon";
            case EXIST: return "triangle";
            case NOTEXIST: return "invtriangle";
        }
        throw new RuntimeException("TODO");
    }

    private Generator(Lang lang, Map<String, String> args) throws CompilerException, FileNotFoundException, IOException {
        var dot = new Dot();


        dot.putGraphAttribute("rankdir", "LR");

        var assets = lang.getAssets();

        var connections = new ArrayList<Con>();

        var mainAsset = assets.get("Application"); // TODO as option? as set of assets?

        var otherClusters = new HashMap<String, Lang.Asset>();

        for (var step : mainAsset.getAttackSteps().values()) {
            //dot.addClusterNode(mainAsset.getName(), step.getName());
            //dot.putClusterAttribute(mainAsset.getName(), "label", mainAsset.getName());

            dot.putNodeAttribute(step.getName(), "shape", stepShape(step));
            dot.putNodeAttribute(step.getName(), "fillcolor", "pink");
            dot.putNodeAttribute(step.getName(), "style", "filled");

            var stepIsInput = false;
            var stepIsOutput = false;

            for (var expr : step.getParentSteps()) {
//                var asf = getAttackStep(expr, new ArrayList<Lang.Field>());
//                var srcStep = asf.attackStep.attackStep;
//                var dstStep = step;
//
//                if (asf.fields.size() == 0) {
//                    continue; // internal edges done later
//                }
//
//                var fieldRef = "";
//                for (var field : asf.fields) {
//                    if (fieldRef.length() != 0) {
//                        fieldRef += ".";
//                    }
//                    fieldRef += field.getName();
//                }
//
//                var stepRef = fieldRef;
//                if (stepRef.length() != 0) {
//                    stepRef += ".";
//                }
//                stepRef += srcStep.getName();
//
//                var srcNode = stepRef.replace(".", "_");
//                dot.addClusterNode(fieldRef.replace(".", "_"), srcNode);
//                dot.putNodeAttribute(srcNode, "label", stepRef);
//
//                dot.putClusterAttribute(fieldRef.replace(".", "_"), "label", fieldRef);
//                var dstNode = dstStep.getName();
//                dot.addEdge(srcNode, dstNode);

                var ref = getAttackStep3(expr);
                //debugln("geh", step.getName() + ":", meh.assetExpr, meh.attackStep.getName());

                if (ref.assetExpr == null) {
                    continue; // internal edges are done later
                }

                stepIsInput = true;

                var srcStep = ref.attackStep;
                var dstStep = step;

                var srcCluster = ref.assetExpr.replace(".", "_d_").replace("(", "_pl_").replace(")", "_pr_").replace("[","_bl_").replace("]", "_br_").replace("\\","_ul_").replace("/","_ur_").replace("-","_sb_");
                var srcClusterName = ref.assetExpr;

                var srcNode = srcCluster + "_a_" + srcStep.getName();
                var dstNode = dstStep.getName();

                dot.addCluster(srcCluster);
                dot.putClusterAttribute(srcCluster, "label", srcClusterName);

                dot.addClusterNode(srcCluster, srcNode);
                dot.putNodeAttribute(srcNode, "label", srcStep.getName());
                dot.putNodeAttribute(srcNode, "shape", stepShape(srcStep));

                otherClusters.put(srcCluster, ref.asset);


                dot.addEdge(srcNode, dstNode);

            }


            // TODO check requires

            for (var expr : step.getReaches()) {
//                var asf = getAttackStep(expr, new ArrayList<Lang.Field>());
//
//                var meh = getAttackStep3(expr); // TODO
//                //debugln("teh", step.getName() + ":", meh);
//
//
//                var srcStep = step;
//                var dstStep = asf.attackStep.attackStep;
//
//                if (asf.fields.size() == 0) { // internal edge
//                    dot.addEdge(srcStep.getName(), dstStep.getName());
//                    continue;
//                }
//
//                var srcNode = srcStep.getName();
//
//                var fieldRef = "";
//                for (var field : asf.fields) {
//                    if (fieldRef.length() != 0) {
//                        fieldRef += ".";
//                    }
//                    fieldRef += field.getName();
//                }
//
//                var stepRef = fieldRef;
//                if (stepRef.length() != 0) {
//                    stepRef += ".";
//                }
//                stepRef += dstStep.getName();
//
//                var dstNode = stepRef.replace(".", "_");
//                dot.addClusterNode(fieldRef.replace(".","_"), dstNode);
//                dot.putNodeAttribute(dstNode, "label", stepRef);
//
//                dot.putClusterAttribute(fieldRef.replace(".", "_"), "label", fieldRef);
//
//                dot.addEdge(srcNode, dstNode);

                var ref = getAttackStep3(expr);
                //debugln("geh", step.getName() + ":", meh.assetExpr, meh.attackStep.getName());

                var srcStep = step;
                var dstStep = ref.attackStep;

                if (ref.assetExpr == null) {
                    var srcNode = srcStep.getName();
                    var dstNode = dstStep.getName();
                    dot.addEdge(srcNode, dstNode);
                    continue;
                }
                stepIsOutput = true;


                var dstCluster = ref.assetExpr.replace(".", "_d_").replace("(", "_pl_").replace(")", "_pr_").replace("[","_bl_").replace("]", "_br_").replace("\\","_ul_").replace("/","_ur_").replace("-","_sb_");
                var dstClusterName = ref.assetExpr;

                var srcNode = srcStep.getName();
                var dstNode = dstCluster + "_a_" + dstStep.getName();

                dot.addCluster(dstCluster);
                dot.putClusterAttribute(dstCluster, "label", dstClusterName);

                dot.addClusterNode(dstCluster, dstNode);
                dot.putNodeAttribute(dstNode, "label", dstStep.getName());

                otherClusters.put(dstCluster, ref.asset);

                dot.addEdge(srcNode, dstNode);

                dot.putNodeAttribute(dstNode, "shape", stepShape(dstStep));
            }

//            if (stepIsInput && stepIsOutput) {
//                //dot.addClusterNode("bi", step.getName());
//                //dot.putClusterAttribute("bi", "label", "bi");
//            } else if (stepIsInput) {
//                //dot.addClusterNode("inputs", step.getName());
//                //dot.putClusterAttribute("inputs", "label", "inputs");
//            }
//            else if (stepIsOutput) {
//                //dot.addClusterNode("outputs", step.getName());
//                //dot.putClusterAttribute("outputs", "label", "outputs");
//            }
//            else {
//                dot.addClusterNode("internal", step.getName());
//                dot.putClusterAttribute("internal", "label", "internal");
//            }

//            dot.addClusterNode("internal", step.getName());
//            dot.putClusterAttribute("internal", "label", "internal");
        }

//        for (var otherCluster : otherClusters.keySet()) {
//            var asset = otherClusters.get(otherCluster);
//            for (var step : mainAsset.getAttackSteps().values()) {
//                for (var expr : step.getReaches()) {
//                    var ref = getAttackStep3(expr);
//
//                    var srcStep = step;
//                    var dstStep = ref.attackStep;
//
//                    if (ref.assetExpr == null) {
//                        var srcNode = otherCluster + "_a_" + srcStep.getName();
//                        var dstNode = otherCluster + "_a_" + dstStep.getName();
//
//
//                        dot.addClusterNode(otherCluster, srcNode);
//                        dot.addClusterNode(otherCluster, dstNode);
//
//                        dot.putNodeAttribute(srcNode, "label", srcStep.getName());
//                        dot.putNodeAttribute(dstNode, "label", dstStep.getName());
//
//                        dot.addEdge(srcNode, dstNode);
//                    }
//                }
//            }
//        }


        var dotFilePath = Paths.get("m1.dot"); // TODO
        dot.writeFile(dotFilePath);
    }


    public void debugln(Object... args) {
        if (args == null) {
            System.err.println("null");
            return;
        }
        var first = true;
        for (var x : args) {
            if (!first) {
                System.err.print(", ");
            }
            if (x == null) {
                System.err.print("null");
            } else {
                System.err.print(x.toString());
            }
            first = false;
        }
        System.err.println();
    }

    public class Ref {
        public String assetExpr;
        public Lang.Asset asset;

        public Lang.AttackStep attackStep;
    }

    private Ref getAttackStep3(Lang.StepExpr expr) { // TODO
        Ref res = new Ref();
        if (expr instanceof Lang.StepCollect) {
            var step = (Lang.StepCollect)expr;

            var lhs = getAttackStep3(step.lhs);
            var rhs = getAttackStep3(step.rhs);

            if (lhs.attackStep != null) {
                throw new RuntimeException("TODO");
            }

            if (rhs.assetExpr == null) {
                res.assetExpr = lhs.assetExpr;
            } else {
                res.assetExpr = String.format("(%s).(%s)", lhs.assetExpr, rhs.assetExpr);
            }
            res.attackStep = rhs.attackStep; // can be null
        } else if (expr instanceof Lang.StepAttackStep) {
            var step = (Lang.StepAttackStep)expr;

            res.assetExpr = null;

            res.attackStep = step.attackStep;
        } else if (expr instanceof Lang.StepField) {
            var step = (Lang.StepField)expr;

            res.assetExpr = step.field.getName();
            res.attackStep = null;
        } else if (expr instanceof Lang.StepCall) {
            var step = (Lang.StepCall)expr;
            //var asset = step.src; // TODO correct?
            var varName = step.name;

            // TODO what if var goes to attacksteps rather than assets? how to know? resolve and discard?
            res.assetExpr = String.format("%s()", varName);
            res.attackStep = null;
        } else if (expr instanceof Lang.StepUnion) {
            var step = (Lang.StepUnion)expr;

            var lhs = getAttackStep3(step.lhs);
            var rhs = getAttackStep3(step.rhs);

            if (lhs.attackStep != null) {
                throw new RuntimeException("TODO");
            }
            if (rhs.attackStep != null) {
                throw new RuntimeException("TODO");
            }

            res.assetExpr = String.format("((%s) \\/ (%s))", lhs.assetExpr, rhs.assetExpr);
            res.attackStep = null;
        } else if (expr instanceof Lang.StepDifference) {
            var step = (Lang.StepDifference)expr;

            var lhs = getAttackStep3(step.lhs);
            var rhs = getAttackStep3(step.rhs);

            if (lhs.attackStep != null) {
                throw new RuntimeException("TODO");
            }
            if (rhs.attackStep != null) {
                throw new RuntimeException("TODO");
            }

            res.assetExpr = String.format("((%s) - (%s))", lhs.assetExpr, rhs.assetExpr);
            res.attackStep = null;
        } else {
            throw new RuntimeException("TODO "+ expr);
        }
        if (expr.target != expr.subTarget) { // TODO accurately recreate SubTypeExpr?
            if (res.attackStep != null) {
                throw new RuntimeException("TODO");
            }
            res.assetExpr = String.format("(%s[%s])", res.assetExpr, expr.subTarget.getName());
        }
        res.asset = expr.subTarget; // TODO split into multiple refs if different types?
        return res;
    }

    private String getAttackStep2(Lang.StepExpr expr) { // TODO
        String res = null;
        if (expr instanceof Lang.StepCollect) {
            var step = (Lang.StepCollect)expr;
            res = getAttackStep2(step.lhs) + "." + getAttackStep2(step.rhs);
        } else if (expr instanceof Lang.StepAttackStep) {
            var step = (Lang.StepAttackStep)expr;

            var attackStep = step.attackStep;
            res = attackStep.getName();
        } else if (expr instanceof Lang.StepField) {
            var step = (Lang.StepField)expr;

            var field = step.field;
            res = field.getName();
        } else if (expr instanceof Lang.StepCall) {
            var step = (Lang.StepCall)expr;
            var asset = step.src; // TODO correct?
            var varName = step.name;

            //var subStep = asset.getVariables().get(varName);
            //res = String.format("(%s)()", getAttackStep2(subStep));
            //
            res = String.format("%s()", varName);
        } else if (expr instanceof Lang.StepUnion) {
            var step = (Lang.StepUnion)expr;
            res = String.format("((%s) \\/ (%s)])", getAttackStep2(step.lhs), getAttackStep2(step.rhs));
        } else if (expr instanceof Lang.StepDifference) {
            var step = (Lang.StepDifference)expr;
            res = String.format("((%s) - (%s)])", getAttackStep2(step.lhs), getAttackStep2(step.rhs));
        } else {
            throw new RuntimeException("TODO "+ expr);
        }
        if (expr.target != expr.subTarget) { // TODO accurately recreate SubTypeExpr?
            res = String.format("(%s[%s])", res, expr.subTarget.getName());
            //debugln(expr, res, (expr.subSrc == null ? null : expr.subSrc.getName()), (expr.src == null ? null : expr.src.getName()), (expr.target == null ? null : expr.target.getName()), (expr.subTarget == null ? null : expr.subTarget.getName()));
        }
        return res;
    }

    private AttackStepField getAttackStep(Lang.StepExpr expr, ArrayList<Lang.Field> fields) {
        addFields(expr, fields);
        if (expr instanceof Lang.StepAttackStep) {
            return new AttackStepField((Lang.StepAttackStep) expr, fields);
        } else if (expr instanceof Lang.StepBinOp) {
            return getAttackStep(((Lang.StepBinOp) expr).rhs, fields);
        } else {
            throw new RuntimeException("Unexpected expression " + expr);
        }
    }

    //Recursive trace all fields connected to
    private void addFields(Lang.StepExpr expr, ArrayList<Lang.Field> fields) {
        if (expr instanceof Lang.StepField) {
            fields.add(((Lang.StepField) expr).field);
        } else if (expr instanceof Lang.StepBinOp) {
            addFields(((Lang.StepBinOp) expr).lhs, fields);
            addFields(((Lang.StepBinOp) expr).rhs, fields);
        } else {
            // TODO
        }
    }

    public static class AttackStepField {
        public final Lang.StepAttackStep attackStep;
        public final ArrayList<Lang.Field> fields;

        public AttackStepField(Lang.StepAttackStep attackStep, ArrayList<Lang.Field> fields) {
            this.attackStep = attackStep;
            this.fields = fields;
        }
    }
}
