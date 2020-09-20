function compareresults(true_loc,est_loc, anchor)
% Function compareresults(true_loc,est_loc, anchors)
% draw both the true location and the estimated locations to 
% compare estimation errors
% Input
%   -- true_loc: True locations of sensors
%   -- est_loc: Estimated locations of sensors
%   -- anchors: anchornodes

if(nargin==2)
    anchor=[];
end

n=size(true_loc,1);

clf;

hold on;
%draw lines connecting true location and estimated locations
for j=1:n
        h= line([true_loc(j,1) est_loc(j,1)], [true_loc(j,2) est_loc(j,2)]);
        set(h,'Color',[0.5 0.5 0.5]);        
end

%Draw points for estimated locations
scatter(est_loc(:,1),est_loc(:,2),49,'k','filled');
%Draw points for true locations
scatter(true_loc(:,1),true_loc(:,2),49,'g','filled');

%Draw points for anchors
if(size(anchor,1)>0)
    scatter(anchor(:,2),anchor(:,3),49,'r','filled');
end

xlabel('X');
ylabel('Y');
axis( [floor(min([est_loc(:,1);true_loc(:,1)]))  ceil(max([est_loc(:,1);true_loc(:,1)])) floor(min([est_loc(:,2);true_loc(:,2)])) ceil(max([est_loc(:,2);true_loc(:,2)])) ] );
hold off;

end