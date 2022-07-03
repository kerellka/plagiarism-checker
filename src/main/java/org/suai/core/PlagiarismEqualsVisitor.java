package org.suai.core;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.modules.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import org.suai.parser.utils.PlagiarismStats;

import java.util.*;

public class PlagiarismEqualsVisitor implements GenericVisitor<Boolean, Visitable> {

    private static final PlagiarismStats stats = PlagiarismStats.getInstance();
    private static final PlagiarismEqualsVisitor SINGLETON = new PlagiarismEqualsVisitor();

    private List<BinaryExpr.Operator> commutativeOperators = List.of(
            BinaryExpr.Operator.OR,
            BinaryExpr.Operator.AND,
            BinaryExpr.Operator.BINARY_OR,
            BinaryExpr.Operator.BINARY_AND,
            BinaryExpr.Operator.XOR,
            BinaryExpr.Operator.EQUALS,
            BinaryExpr.Operator.NOT_EQUALS,
            BinaryExpr.Operator.PLUS,
            BinaryExpr.Operator.MULTIPLY);


    public static boolean equals(final Node n, final Node n2) {
        return SINGLETON.nodeEquals(n, n2);
    }

    public static long countPlagiarismPercentage(final Node n, final Node n2) {
        stats.refreshStats();
        SINGLETON.nodeEquals(n, n2);
        return stats.countPlagiarismPercent();
    }

    private PlagiarismEqualsVisitor() { }

    private <T extends Node> boolean nodesEquals(final List<T> nodes1, final List<T> nodes2) {
        if (nodes1 == null) {
            return nodes2 == null;
        } else if (nodes2 == null) {
            return false;
        }
        if (nodes1.size() != nodes2.size()) {
            return false;
        }
        for (int i = 0; i < nodes1.size(); i++) {
            if (!nodeEquals(nodes1.get(i), nodes2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private <N extends Node> boolean nodesEquals(NodeList<N> n1, NodeList<N> n2) {
        if (n1 == n2) {
            return true;
        }
        if (n1 == null || n2 == null) {
            return false;
        }

        HashMap<Node, Node> hasTwin1 = new HashMap<>();
        HashMap<Node, Integer> levenstainDistances = new HashMap<>();
        n1.forEach(nEl -> hasTwin1.put(nEl, null));
        n1.forEach(n1Element -> {
            for (Node n2Element : n2) {
                if (nodeEquals(n1Element, n2Element)) {
                    hasTwin1.put(n1Element, n2Element);
                    break;
                } else if (isMethodDeclarations(n1Element, n2Element)) {
                    levenstainDistances.put(n2Element, levenstainDistance((MethodDeclaration) n1Element, (MethodDeclaration) n2Element));
                }
            }
            var candidateTwin = levenstainDistances.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() <= ((MethodDeclaration) n1Element).getBody().get().stream().count() / 2)
                    .findFirst();
            candidateTwin.ifPresent(nodeIntegerEntry -> hasTwin1.put(n1Element, nodeIntegerEntry.getKey()));
        });

        hasTwin1.keySet().forEach(stats::addNode);
        hasTwin1.entrySet().stream().filter(e -> e.getValue() != null).forEach(n -> stats.addHasTwin(n.getKey()));
        return hasTwin1.values().stream().allMatch(Objects::nonNull);
    }

    private <T extends Node> boolean nodeEquals(final T n, final T n2) {
        if (n == n2) {
            return true;
        }
        if (n == null || n2 == null) {
            return false;
        }
        if (n.getClass() != n2.getClass()) {
            return false;
        }
        if (!commonNodeEquality(n, n2)) {
            return false;
        }
        return n.accept(this, n2);
    }

    private boolean commonNodeEquality(Node n, Node n2) {
        if (!nodeEquals(n.getComment(), n2.getComment())) {
            return false;
        }
        return nodesEquals(n.getOrphanComments(), n2.getOrphanComments());
    }

    private <T extends Node> boolean nodeEquals(final Optional<T> n, final Optional<T> n2) {
        return nodeEquals(n.orElse(null), n2.orElse(null));
    }

    private <T extends Node> boolean nodesEquals(final Optional<NodeList<T>> n, final Optional<NodeList<T>> n2) {
        return nodesEquals(n.orElse(null), n2.orElse(null));
    }

    private boolean objEquals(final Object n, final Object n2) {
        if (n == n2) {
            return true;
        }
        if (n == null || n2 == null) {
            return false;
        }
        return n.equals(n2);
    }

    private int levenstainDistance(MethodDeclaration method1, MethodDeclaration method2) {
        return levenstainDistance(method1.getBody().get().getStatements(), method2.getBody().get().getStatements());
    }

    private int levenstainDistance(List<? extends Node> block1, List<? extends Node> block2) {
        int[] Di_1 = new int[block2.size() + 1];
        int[] Di = new int[block2.size() + 1];

        for (int j = 0; j <= block2.size(); j++) {
            Di[j] = j;
        }

        for (int i = 1; i <= block1.size(); i++) {
            System.arraycopy(Di, 0, Di_1, 0, Di_1.length);

            Di[0] = i;
            for (int j = 1; j <= block2.size(); j++) {
                int cost = nodeEquals(block1.get(i - 1), block2.get(j - 1)) ? 0 : 1;
                Di[j] = min(
                        Di_1[j] + 1,
                        Di[j - 1] + 1,
                        Di_1[j - 1] + cost
                );
            }
        }

        return Di[Di.length - 1];
    }

    private int min(int n1, int n2, int n3) {
        return Math.min(Math.min(n1, n2), n3);
    }

    private boolean isCommutativeSwap(BinaryExpr expr1, BinaryExpr expr2) {
        if (commutativeOperators.contains(expr1.getOperator()) && commutativeOperators.contains(expr2.getOperator())) {
            if (nodeEquals(expr1.getLeft(), expr2.getRight()) &&
                    nodeEquals(expr1.getRight(), expr2.getLeft())) {
                return true;
            }
        }
        return false;
    }

    private boolean isMethodDeclarations(Node el1, Node el2) {
        return el1.getClass().equals(MethodDeclaration.class) && el2.getClass().equals(MethodDeclaration.class);
    }

    @Override
    public Boolean visit(final CompilationUnit n, final Visitable arg) {
        final CompilationUnit n2 = (CompilationUnit) arg;
        if (!nodesEquals(n.getImports(), n2.getImports()))
            return false;
        if (!nodeEquals(n.getModule(), n2.getModule()))
            return false;
        if (!nodeEquals(n.getPackageDeclaration(), n2.getPackageDeclaration()))
            return false;
        if (!nodesEquals(n.getTypes(), n2.getTypes()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final PackageDeclaration n, final Visitable arg) {
        return true;
    }

    @Override
    public Boolean visit(final TypeParameter n, final Visitable arg) {
        final TypeParameter n2 = (TypeParameter) arg;
        if (!nodesEquals(n.getTypeBound(), n2.getTypeBound()))
            return false;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final LineComment n, final Visitable arg) {
        return true;
    }

    @Override
    public Boolean visit(final BlockComment n, final Visitable arg) {
        return true;
    }

    @Override
    public Boolean visit(final ClassOrInterfaceDeclaration n, final Visitable arg) {
        final ClassOrInterfaceDeclaration n2 = (ClassOrInterfaceDeclaration) arg;
        if (!nodesEquals(n.getExtendedTypes(), n2.getExtendedTypes()))
            return false;
        if (!nodesEquals(n.getImplementedTypes(), n2.getImplementedTypes()))
            return false;
        if (!objEquals(n.isInterface(), n2.isInterface()))
            return false;
        if (!nodesEquals(n.getTypeParameters(), n2.getTypeParameters()))
            return false;
        if (!nodesEquals(n.getMembers(), n2.getMembers()))
            return false;
        if (!nodesEquals(n.getModifiers(), n2.getModifiers()))
            return false;

        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final EnumDeclaration n, final Visitable arg) {
        final EnumDeclaration n2 = (EnumDeclaration) arg;
        if (!nodesEquals(n.getEntries(), n2.getEntries()))
            return false;
        if (!nodesEquals(n.getImplementedTypes(), n2.getImplementedTypes()))
            return false;
        if (!nodesEquals(n.getMembers(), n2.getMembers()))
            return false;
        if (!nodesEquals(n.getModifiers(), n2.getModifiers()))
            return false;

        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final EnumConstantDeclaration n, final Visitable arg) {
        final EnumConstantDeclaration n2 = (EnumConstantDeclaration) arg;
        if (!nodesEquals(n.getArguments(), n2.getArguments()))
            return false;
        if (!nodesEquals(n.getClassBody(), n2.getClassBody()))
            return false;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final AnnotationDeclaration n, final Visitable arg) {
        final AnnotationDeclaration n2 = (AnnotationDeclaration) arg;
        if (!nodesEquals(n.getMembers(), n2.getMembers()))
            return false;
        if (!nodesEquals(n.getModifiers(), n2.getModifiers()))
            return false;

        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final AnnotationMemberDeclaration n, final Visitable arg) {
        final AnnotationMemberDeclaration n2 = (AnnotationMemberDeclaration) arg;
        if (!nodeEquals(n.getDefaultValue(), n2.getDefaultValue()))
            return false;
        if (!nodesEquals(n.getModifiers(), n2.getModifiers()))
            return false;

        if (!nodeEquals(n.getType(), n2.getType()))
            return false;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final FieldDeclaration n, final Visitable arg) {
        final FieldDeclaration n2 = (FieldDeclaration) arg;
        if (!nodesEquals(n.getModifiers(), n2.getModifiers()))
            return false;
        if (!nodesEquals(n.getVariables(), n2.getVariables()))
            return false;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final VariableDeclarator n, final Visitable arg) {
        final VariableDeclarator n2 = (VariableDeclarator) arg;
        if (!nodeEquals(n.getInitializer(), n2.getInitializer()))
            return false;

        if (!nodeEquals(n.getType(), n2.getType()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ConstructorDeclaration n, final Visitable arg) {
        final ConstructorDeclaration n2 = (ConstructorDeclaration) arg;
        if (!nodeEquals(n.getBody(), n2.getBody()))
            return false;
        if (!nodesEquals(n.getModifiers(), n2.getModifiers()))
            return false;

        if (!nodesEquals(n.getParameters(), n2.getParameters()))
            return false;
        if (!nodeEquals(n.getReceiverParameter(), n2.getReceiverParameter()))
            return false;
        if (!nodesEquals(n.getThrownExceptions(), n2.getThrownExceptions()))
            return false;
        if (!nodesEquals(n.getTypeParameters(), n2.getTypeParameters()))
            return false;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final MethodDeclaration n, final Visitable arg) {
        final MethodDeclaration n2 = (MethodDeclaration) arg;
        if (!nodeEquals(n.getBody(), n2.getBody()))
            return false;
        if (!nodeEquals(n.getType(), n2.getType()))
            return false;
        if (!nodesEquals(n.getModifiers(), n2.getModifiers()))
            return false;

        if (!nodesEquals(n.getParameters(), n2.getParameters()))
            return false;
        if (!nodeEquals(n.getReceiverParameter(), n2.getReceiverParameter()))
            return false;
        if (!nodesEquals(n.getThrownExceptions(), n2.getThrownExceptions()))
            return false;
        if (!nodesEquals(n.getTypeParameters(), n2.getTypeParameters()))
            return false;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final Parameter n, final Visitable arg) {
        final Parameter n2 = (Parameter) arg;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!objEquals(n.isVarArgs(), n2.isVarArgs()))
            return false;
        if (!nodesEquals(n.getModifiers(), n2.getModifiers()))
            return false;

        if (!nodeEquals(n.getType(), n2.getType()))
            return false;
        if (!nodesEquals(n.getVarArgsAnnotations(), n2.getVarArgsAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final InitializerDeclaration n, final Visitable arg) {
        final InitializerDeclaration n2 = (InitializerDeclaration) arg;
        if (!nodeEquals(n.getBody(), n2.getBody()))
            return false;
        if (!objEquals(n.isStatic(), n2.isStatic()))
            return false;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final JavadocComment n, final Visitable arg) {
        return true;
    }

    @Override
    public Boolean visit(final ClassOrInterfaceType n, final Visitable arg) {
        final ClassOrInterfaceType n2 = (ClassOrInterfaceType) arg;

        if (!nodeEquals(n.getScope(), n2.getScope()))
            return false;
        if (!nodesEquals(n.getTypeArguments(), n2.getTypeArguments()))
            return false;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final PrimitiveType n, final Visitable arg) {
        final PrimitiveType n2 = (PrimitiveType) arg;
        if (!objEquals(n.getType(), n2.getType()))
            return false;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ArrayType n, final Visitable arg) {
        final ArrayType n2 = (ArrayType) arg;
        if (!nodeEquals(n.getComponentType(), n2.getComponentType()))
            return false;
        if (!objEquals(n.getOrigin(), n2.getOrigin()))
            return false;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ArrayCreationLevel n, final Visitable arg) {
        final ArrayCreationLevel n2 = (ArrayCreationLevel) arg;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getDimension(), n2.getDimension()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final IntersectionType n, final Visitable arg) {
        final IntersectionType n2 = (IntersectionType) arg;
        if (!nodesEquals(n.getElements(), n2.getElements()))
            return false;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final UnionType n, final Visitable arg) {
        final UnionType n2 = (UnionType) arg;
        if (!nodesEquals(n.getElements(), n2.getElements()))
            return false;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final VoidType n, final Visitable arg) {
        final VoidType n2 = (VoidType) arg;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final WildcardType n, final Visitable arg) {
        final WildcardType n2 = (WildcardType) arg;
        if (!nodeEquals(n.getExtendedType(), n2.getExtendedType()))
            return false;
        if (!nodeEquals(n.getSuperType(), n2.getSuperType()))
            return false;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final UnknownType n, final Visitable arg) {
        final UnknownType n2 = (UnknownType) arg;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ArrayAccessExpr n, final Visitable arg) {
        final ArrayAccessExpr n2 = (ArrayAccessExpr) arg;
        if (!nodeEquals(n.getIndex(), n2.getIndex()))
            return false;

        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ArrayCreationExpr n, final Visitable arg) {
        final ArrayCreationExpr n2 = (ArrayCreationExpr) arg;
        if (!nodeEquals(n.getElementType(), n2.getElementType()))
            return false;
        if (!nodeEquals(n.getInitializer(), n2.getInitializer()))
            return false;
        if (!nodesEquals(n.getLevels(), n2.getLevels()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ArrayInitializerExpr n, final Visitable arg) {
        final ArrayInitializerExpr n2 = (ArrayInitializerExpr) arg;
        if (!nodesEquals(n.getValues(), n2.getValues()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final AssignExpr n, final Visitable arg) {
        final AssignExpr n2 = (AssignExpr) arg;
        if (!objEquals(n.getOperator(), n2.getOperator()))
            return false;
        if (!nodeEquals(n.getTarget(), n2.getTarget()))
            return false;
        if (!nodeEquals(n.getValue(), n2.getValue()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final BinaryExpr n, final Visitable arg) {
        final BinaryExpr n2 = (BinaryExpr) arg;
        if (!objEquals(n.getOperator(), n2.getOperator())) {
            return false;
        }
        if (isCommutativeSwap(n, n2)) {
            return true;
        }
        if (!nodeEquals(n.getLeft(), n2.getLeft())) {
            return false;
        }
        if (!nodeEquals(n.getRight(), n2.getRight())) {
            return false;
        }
        return true;
    }

    @Override
    public Boolean visit(final CastExpr n, final Visitable arg) {
        final CastExpr n2 = (CastExpr) arg;
        if (!nodeEquals(n.getExpression(), n2.getExpression()))
            return false;
        if (!nodeEquals(n.getType(), n2.getType()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ClassExpr n, final Visitable arg) {
        final ClassExpr n2 = (ClassExpr) arg;
        if (!nodeEquals(n.getType(), n2.getType()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ConditionalExpr n, final Visitable arg) {
        final ConditionalExpr n2 = (ConditionalExpr) arg;
        if (!nodeEquals(n.getCondition(), n2.getCondition()))
            return false;
        if (!nodeEquals(n.getElseExpr(), n2.getElseExpr()))
            return false;
        if (!nodeEquals(n.getThenExpr(), n2.getThenExpr()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final EnclosedExpr n, final Visitable arg) {
        final EnclosedExpr n2 = (EnclosedExpr) arg;
        if (!nodeEquals(n.getInner(), n2.getInner()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final FieldAccessExpr n, final Visitable arg) {
        final FieldAccessExpr n2 = (FieldAccessExpr) arg;

        if (!nodeEquals(n.getScope(), n2.getScope()))
            return false;
        if (!nodesEquals(n.getTypeArguments(), n2.getTypeArguments()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final InstanceOfExpr n, final Visitable arg) {
        final InstanceOfExpr n2 = (InstanceOfExpr) arg;
        if (!nodeEquals(n.getExpression(), n2.getExpression()))
            return false;
        if (!nodeEquals(n.getPattern(), n2.getPattern()))
            return false;
        if (!nodeEquals(n.getType(), n2.getType()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final StringLiteralExpr n, final Visitable arg) {
        final StringLiteralExpr n2 = (StringLiteralExpr) arg;
        if (!objEquals(n.getValue(), n2.getValue()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final IntegerLiteralExpr n, final Visitable arg) {
        final IntegerLiteralExpr n2 = (IntegerLiteralExpr) arg;
        if (!objEquals(n.getValue(), n2.getValue()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final LongLiteralExpr n, final Visitable arg) {
        final LongLiteralExpr n2 = (LongLiteralExpr) arg;
        if (!objEquals(n.getValue(), n2.getValue()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final CharLiteralExpr n, final Visitable arg) {
        final CharLiteralExpr n2 = (CharLiteralExpr) arg;
        if (!objEquals(n.getValue(), n2.getValue()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final DoubleLiteralExpr n, final Visitable arg) {
        final DoubleLiteralExpr n2 = (DoubleLiteralExpr) arg;
        if (!objEquals(n.getValue(), n2.getValue()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final BooleanLiteralExpr n, final Visitable arg) {
        final BooleanLiteralExpr n2 = (BooleanLiteralExpr) arg;
        if (!objEquals(n.isValue(), n2.isValue()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final NullLiteralExpr n, final Visitable arg) {
        final NullLiteralExpr n2 = (NullLiteralExpr) arg;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final MethodCallExpr n, final Visitable arg) {
        final MethodCallExpr n2 = (MethodCallExpr) arg;
        if (!nodesEquals(n.getArguments(), n2.getArguments()))
            return false;

        if (!nodeEquals(n.getScope(), n2.getScope()))
            return false;
        if (!nodesEquals(n.getTypeArguments(), n2.getTypeArguments()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final NameExpr n, final Visitable arg) {
        final NameExpr n2 = (NameExpr) arg;

        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ObjectCreationExpr n, final Visitable arg) {
        final ObjectCreationExpr n2 = (ObjectCreationExpr) arg;
        if (!nodesEquals(n.getAnonymousClassBody(), n2.getAnonymousClassBody()))
            return false;
        if (!nodesEquals(n.getArguments(), n2.getArguments()))
            return false;
        if (!nodeEquals(n.getScope(), n2.getScope()))
            return false;
        if (!nodeEquals(n.getType(), n2.getType()))
            return false;
        if (!nodesEquals(n.getTypeArguments(), n2.getTypeArguments()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final Name n, final Visitable arg) {
        return true;
    }

    @Override
    public Boolean visit(final SimpleName n, final Visitable arg) {
        return true;
    }

    @Override
    public Boolean visit(final ThisExpr n, final Visitable arg) {
        final ThisExpr n2 = (ThisExpr) arg;
        if (!nodeEquals(n.getTypeName(), n2.getTypeName()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final SuperExpr n, final Visitable arg) {
        final SuperExpr n2 = (SuperExpr) arg;
        if (!nodeEquals(n.getTypeName(), n2.getTypeName()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final UnaryExpr n, final Visitable arg) {
        final UnaryExpr n2 = (UnaryExpr) arg;
        if (!nodeEquals(n.getExpression(), n2.getExpression()))
            return false;
        if (!objEquals(n.getOperator(), n2.getOperator()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final VariableDeclarationExpr n, final Visitable arg) {
        final VariableDeclarationExpr n2 = (VariableDeclarationExpr) arg;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodesEquals(n.getModifiers(), n2.getModifiers()))
            return false;
        if (!nodesEquals(n.getVariables(), n2.getVariables()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final MarkerAnnotationExpr n, final Visitable arg) {
        final MarkerAnnotationExpr n2 = (MarkerAnnotationExpr) arg;

        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final SingleMemberAnnotationExpr n, final Visitable arg) {
        final SingleMemberAnnotationExpr n2 = (SingleMemberAnnotationExpr) arg;
        if (!nodeEquals(n.getMemberValue(), n2.getMemberValue()))
            return false;

        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final NormalAnnotationExpr n, final Visitable arg) {
        final NormalAnnotationExpr n2 = (NormalAnnotationExpr) arg;
        if (!nodesEquals(n.getPairs(), n2.getPairs()))
            return false;

        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final MemberValuePair n, final Visitable arg) {
        final MemberValuePair n2 = (MemberValuePair) arg;

        if (!nodeEquals(n.getValue(), n2.getValue()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ExplicitConstructorInvocationStmt n, final Visitable arg) {
        final ExplicitConstructorInvocationStmt n2 = (ExplicitConstructorInvocationStmt) arg;
        if (!nodesEquals(n.getArguments(), n2.getArguments()))
            return false;
        if (!nodeEquals(n.getExpression(), n2.getExpression()))
            return false;
        if (!objEquals(n.isThis(), n2.isThis()))
            return false;
        if (!nodesEquals(n.getTypeArguments(), n2.getTypeArguments()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final LocalClassDeclarationStmt n, final Visitable arg) {
        final LocalClassDeclarationStmt n2 = (LocalClassDeclarationStmt) arg;
        if (!nodeEquals(n.getClassDeclaration(), n2.getClassDeclaration()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final LocalRecordDeclarationStmt n, final Visitable arg) {
        final LocalRecordDeclarationStmt n2 = (LocalRecordDeclarationStmt) arg;
        if (!nodeEquals(n.getRecordDeclaration(), n2.getRecordDeclaration()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final AssertStmt n, final Visitable arg) {
        final AssertStmt n2 = (AssertStmt) arg;
        if (!nodeEquals(n.getCheck(), n2.getCheck()))
            return false;
        if (!nodeEquals(n.getMessage(), n2.getMessage()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final BlockStmt n, final Visitable arg) {
        final BlockStmt n2 = (BlockStmt) arg;
        if (!nodesEquals(n.getStatements(), n2.getStatements()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final LabeledStmt n, final Visitable arg) {
        final LabeledStmt n2 = (LabeledStmt) arg;
        if (!nodeEquals(n.getLabel(), n2.getLabel()))
            return false;
        if (!nodeEquals(n.getStatement(), n2.getStatement()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final EmptyStmt n, final Visitable arg) {
        final EmptyStmt n2 = (EmptyStmt) arg;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ExpressionStmt n, final Visitable arg) {
        final ExpressionStmt n2 = (ExpressionStmt) arg;
        if (!nodeEquals(n.getExpression(), n2.getExpression()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final SwitchStmt n, final Visitable arg) {
        final SwitchStmt n2 = (SwitchStmt) arg;
        if (!nodesEquals(n.getEntries(), n2.getEntries()))
            return false;
        if (!nodeEquals(n.getSelector(), n2.getSelector()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final SwitchEntry n, final Visitable arg) {
        final SwitchEntry n2 = (SwitchEntry) arg;
        if (!nodesEquals(n.getLabels(), n2.getLabels()))
            return false;
        if (!nodesEquals(n.getStatements(), n2.getStatements()))
            return false;
        if (!objEquals(n.getType(), n2.getType()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final BreakStmt n, final Visitable arg) {
        final BreakStmt n2 = (BreakStmt) arg;
        if (!nodeEquals(n.getLabel(), n2.getLabel()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ReturnStmt n, final Visitable arg) {
        final ReturnStmt n2 = (ReturnStmt) arg;
        if (!nodeEquals(n.getExpression(), n2.getExpression()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final IfStmt n, final Visitable arg) {
        final IfStmt n2 = (IfStmt) arg;
        if (!nodeEquals(n.getCondition(), n2.getCondition()))
            return false;
        if (!nodeEquals(n.getElseStmt(), n2.getElseStmt()))
            return false;
        if (!nodeEquals(n.getThenStmt(), n2.getThenStmt()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final WhileStmt n, final Visitable arg) {
        final WhileStmt n2 = (WhileStmt) arg;
        if (!nodeEquals(n.getBody(), n2.getBody()))
            return false;
        if (!nodeEquals(n.getCondition(), n2.getCondition()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ContinueStmt n, final Visitable arg) {
        final ContinueStmt n2 = (ContinueStmt) arg;
        if (!nodeEquals(n.getLabel(), n2.getLabel()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final DoStmt n, final Visitable arg) {
        final DoStmt n2 = (DoStmt) arg;
        if (!nodeEquals(n.getBody(), n2.getBody()))
            return false;
        if (!nodeEquals(n.getCondition(), n2.getCondition()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ForEachStmt n, final Visitable arg) {
        final ForEachStmt n2 = (ForEachStmt) arg;
        if (!nodeEquals(n.getBody(), n2.getBody()))
            return false;
        if (!nodeEquals(n.getIterable(), n2.getIterable()))
            return false;
        if (!nodeEquals(n.getVariable(), n2.getVariable()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ForStmt n, final Visitable arg) {
        final ForStmt n2 = (ForStmt) arg;
        if (!nodeEquals(n.getBody(), n2.getBody()))
            return false;
        if (!nodeEquals(n.getCompare(), n2.getCompare()))
            return false;
        if (!nodesEquals(n.getInitialization(), n2.getInitialization()))
            return false;
        if (!nodesEquals(n.getUpdate(), n2.getUpdate()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ThrowStmt n, final Visitable arg) {
        final ThrowStmt n2 = (ThrowStmt) arg;
        if (!nodeEquals(n.getExpression(), n2.getExpression()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final SynchronizedStmt n, final Visitable arg) {
        final SynchronizedStmt n2 = (SynchronizedStmt) arg;
        if (!nodeEquals(n.getBody(), n2.getBody()))
            return false;
        if (!nodeEquals(n.getExpression(), n2.getExpression()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final TryStmt n, final Visitable arg) {
        final TryStmt n2 = (TryStmt) arg;
        if (!nodesEquals(n.getCatchClauses(), n2.getCatchClauses()))
            return false;
        if (!nodeEquals(n.getFinallyBlock(), n2.getFinallyBlock()))
            return false;
        if (!nodesEquals(n.getResources(), n2.getResources()))
            return false;
        if (!nodeEquals(n.getTryBlock(), n2.getTryBlock()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final CatchClause n, final Visitable arg) {
        final CatchClause n2 = (CatchClause) arg;
        if (!nodeEquals(n.getBody(), n2.getBody()))
            return false;
        if (!nodeEquals(n.getParameter(), n2.getParameter()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final LambdaExpr n, final Visitable arg) {
        final LambdaExpr n2 = (LambdaExpr) arg;
        if (!nodeEquals(n.getBody(), n2.getBody()))
            return false;
        if (!objEquals(n.isEnclosingParameters(), n2.isEnclosingParameters()))
            return false;
        if (!nodesEquals(n.getParameters(), n2.getParameters()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final MethodReferenceExpr n, final Visitable arg) {
        final MethodReferenceExpr n2 = (MethodReferenceExpr) arg;
        if (!objEquals(n.getIdentifier(), n2.getIdentifier()))
            return false;
        if (!nodeEquals(n.getScope(), n2.getScope()))
            return false;
        if (!nodesEquals(n.getTypeArguments(), n2.getTypeArguments()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final TypeExpr n, final Visitable arg) {
        final TypeExpr n2 = (TypeExpr) arg;
        if (!nodeEquals(n.getType(), n2.getType()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ImportDeclaration n, final Visitable arg) {
        final ImportDeclaration n2 = (ImportDeclaration) arg;
        if (!objEquals(n.isAsterisk(), n2.isAsterisk()))
            return false;
        if (!objEquals(n.isStatic(), n2.isStatic()))
            return false;

        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(NodeList n, Visitable arg) {
        return nodesEquals((NodeList<Node>) n, (NodeList<Node>) arg);
    }

    @Override
    public Boolean visit(final ModuleDeclaration n, final Visitable arg) {
        final ModuleDeclaration n2 = (ModuleDeclaration) arg;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodesEquals(n.getDirectives(), n2.getDirectives()))
            return false;
        if (!objEquals(n.isOpen(), n2.isOpen()))
            return false;

        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ModuleRequiresDirective n, final Visitable arg) {
        final ModuleRequiresDirective n2 = (ModuleRequiresDirective) arg;
        if (!nodesEquals(n.getModifiers(), n2.getModifiers()))
            return false;

        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override()
    public Boolean visit(final ModuleExportsDirective n, final Visitable arg) {
        final ModuleExportsDirective n2 = (ModuleExportsDirective) arg;
        if (!nodesEquals(n.getModuleNames(), n2.getModuleNames()))
            return false;

        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override()
    public Boolean visit(final ModuleProvidesDirective n, final Visitable arg) {
        final ModuleProvidesDirective n2 = (ModuleProvidesDirective) arg;

        if (!nodesEquals(n.getWith(), n2.getWith()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override()
    public Boolean visit(final ModuleUsesDirective n, final Visitable arg) {
        final ModuleUsesDirective n2 = (ModuleUsesDirective) arg;

        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ModuleOpensDirective n, final Visitable arg) {
        final ModuleOpensDirective n2 = (ModuleOpensDirective) arg;
        if (!nodesEquals(n.getModuleNames(), n2.getModuleNames()))
            return false;

        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final UnparsableStmt n, final Visitable arg) {
        final UnparsableStmt n2 = (UnparsableStmt) arg;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final ReceiverParameter n, final Visitable arg) {
        final ReceiverParameter n2 = (ReceiverParameter) arg;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;

        if (!nodeEquals(n.getType(), n2.getType()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final VarType n, final Visitable arg) {
        final VarType n2 = (VarType) arg;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final Modifier n, final Visitable arg) {
        final Modifier n2 = (Modifier) arg;
        if (!objEquals(n.getKeyword(), n2.getKeyword()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final SwitchExpr n, final Visitable arg) {
        final SwitchExpr n2 = (SwitchExpr) arg;
        if (!nodesEquals(n.getEntries(), n2.getEntries()))
            return false;
        if (!nodeEquals(n.getSelector(), n2.getSelector()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final YieldStmt n, final Visitable arg) {
        final YieldStmt n2 = (YieldStmt) arg;
        if (!nodeEquals(n.getExpression(), n2.getExpression()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final TextBlockLiteralExpr n, final Visitable arg) {
        final TextBlockLiteralExpr n2 = (TextBlockLiteralExpr) arg;
        if (!objEquals(n.getValue(), n2.getValue()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final PatternExpr n, final Visitable arg) {
        final PatternExpr n2 = (PatternExpr) arg;

        if (!nodeEquals(n.getType(), n2.getType()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final RecordDeclaration n, final Visitable arg) {
        final RecordDeclaration n2 = (RecordDeclaration) arg;
        if (!nodesEquals(n.getImplementedTypes(), n2.getImplementedTypes()))
            return false;
        if (!nodesEquals(n.getParameters(), n2.getParameters()))
            return false;
        if (!nodeEquals(n.getReceiverParameter(), n2.getReceiverParameter()))
            return false;
        if (!nodesEquals(n.getTypeParameters(), n2.getTypeParameters()))
            return false;
        if (!nodesEquals(n.getMembers(), n2.getMembers()))
            return false;
        if (!nodesEquals(n.getModifiers(), n2.getModifiers()))
            return false;

        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }

    @Override
    public Boolean visit(final CompactConstructorDeclaration n, final Visitable arg) {
        final CompactConstructorDeclaration n2 = (CompactConstructorDeclaration) arg;
        if (!nodeEquals(n.getBody(), n2.getBody()))
            return false;
        if (!nodesEquals(n.getModifiers(), n2.getModifiers()))
            return false;

        if (!nodesEquals(n.getThrownExceptions(), n2.getThrownExceptions()))
            return false;
        if (!nodesEquals(n.getTypeParameters(), n2.getTypeParameters()))
            return false;
        if (!nodesEquals(n.getAnnotations(), n2.getAnnotations()))
            return false;
        if (!nodeEquals(n.getComment(), n2.getComment()))
            return false;
        return true;
    }
}
